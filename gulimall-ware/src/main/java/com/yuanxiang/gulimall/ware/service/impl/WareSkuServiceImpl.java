package com.yuanxiang.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.yuanxiang.common.exception.NoStockException;
import com.yuanxiang.common.to.StockDetailTo;
import com.yuanxiang.common.to.StockLockedTo;
import com.yuanxiang.common.to.mq.OrderTo;
import com.yuanxiang.common.utils.R;
import com.yuanxiang.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.yuanxiang.gulimall.ware.entity.WareOrderTaskEntity;
import com.yuanxiang.gulimall.ware.feign.OrderFeignService;
import com.yuanxiang.gulimall.ware.feign.ProductFeignService;
import com.yuanxiang.gulimall.ware.service.WareOrderTaskDetailService;
import com.yuanxiang.gulimall.ware.service.WareOrderTaskService;
import com.yuanxiang.gulimall.ware.vo.OrderItemVo;
import com.yuanxiang.gulimall.ware.vo.OrderVo;
import com.yuanxiang.gulimall.ware.vo.SkuHasStockVo;
import com.yuanxiang.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.common.utils.Query;

import com.yuanxiang.gulimall.ware.dao.WareSkuDao;
import com.yuanxiang.gulimall.ware.entity.WareSkuEntity;
import com.yuanxiang.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    //skuId:
    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    OrderFeignService orderFeignService;


    //解锁库存
    public void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        //库存解锁
        wareSkuDao.unlockSkuStock(skuId, wareId, num);
        //更新库存单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(entity);
    }

    //wareId:
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);

        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            wareSkuEntity.setSkuName("");
            //远程调用后出现异常  不让它回滚
            //1、自己catch异常
            //TODO 还有什么办法能让他不回滚
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
            }
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> vos = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();

            Long stock = baseMapper.getSkuStock(skuId);

            vo.setSkuId(skuId);
            vo.setHasStock(stock == null ? false : stock > 0);
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }

    /**
     * 锁定库存
     * 锁定库存的场景
     * 1 下订单成功,当但过期没有支付 被系统自动取消,被用户手动取消.都要解锁
     * 2  下订单成功,库存锁定失败,接下来的业务失败
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        //保存工作单详情
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);
        //1、按照下单的地址，找到一个就近的仓库，锁定库存
        //1、找到每个商品在哪个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());//前面漏了这关键的一步  导致锁库存的update语句一直返回0  而且也完全没有锁住库存
            //查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        //2、锁定库存
        for (SkuWareHasStock hasStock : collect) {
            boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (StringUtils.isEmpty(wareIds)) {
                //没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            //1、如果每一个商品都锁定成功,将当前商品锁定了几件的工作单记录发给MQ
            //2、锁定失败。前面保存的工作单信息都回滚了。发送出去的消息，即使要解锁库存，由于在数据库查不到指定的id，所有就不用解锁
            for (Long wareId : wareIds) {
                //锁定成功就返回1，失败就返回0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(taskDetailEntity);

                    //TODO 告诉MQ库存锁定成功
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity, detailTo);
                    lockedTo.setDetail(detailTo);
                    //与库存的延迟队列绑定
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                } else {
                    //当前仓库锁失败，重试下一个仓库
                }
                if (skuStocked == false) {
                    throw new NoStockException(skuId);
                }
            }
        }
        return true;

    }

    @Override
    public void unlockStock(StockLockedTo to) {
        StockDetailTo detailTo = to.getDetail();
        Long detailId = detailTo.getId();
        //解锁
        //1 查询数据库关于这个订单的锁定库存信息
        /*
        有 证明库存锁定成功
            解锁:订单情况
                1\没有这个订单,必须解锁
                2\有这个订单,不能解锁库存
                    订单状态:已取消:解锁库存
                            没取消:不能解锁
         没有 库存锁定失败,库存回滚,这种情况不用解锁
         */
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if (byId != null) {
            //解锁
            Long id = to.getId();//库存工作单的id
            WareOrderTaskEntity byId1 = wareOrderTaskService.getById(id);//根据库存工作单id查到库存订单
            String orderSn = byId1.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                Integer status = data.getStatus();//这里显示status为0 status变化的原因是什么?
                System.out.println(data.toString());
                if (data == null || status == 4) {
                    //订单已经被取消了 此时要解锁库存
                    if (byId.getLockStatus() == 1) {
                        unLockStock(detailTo.getSkuId(), detailTo.getWareId(), detailTo.getSkuNum(), detailId);
                    }
                    //手动
                }
            } else {
                //消息拒绝以后,重新发回队列,让别人继续消费解锁
                throw new RuntimeException("远程服务失败");
            }
        } else {
            //无需解锁
        }
    }

    //防止订单服务卡顿导致订单消息一直改不了,库存消息优先到期.查订单状态为新建状态.
    //导致卡顿的订单永远解锁不了
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查一下最新的库存状态
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getOrderTaskEntity(orderSn);
        Long id = wareOrderTaskEntity.getId();
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : list) {
            unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
        }
    }


    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }


}