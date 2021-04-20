package com.yuanxiang.gulimall.ware.service.impl;

import com.yuanxiang.common.constant.WareConstant;
import com.yuanxiang.gulimall.ware.entity.PurchaseDetailEntity;
import com.yuanxiang.gulimall.ware.service.PurchaseDetailService;
import com.yuanxiang.gulimall.ware.service.WareSkuService;
import com.yuanxiang.gulimall.ware.vo.MergeVo;
import com.yuanxiang.gulimall.ware.vo.PurchaseDoneVo;
import com.yuanxiang.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.common.utils.Query;

import com.yuanxiang.gulimall.ware.dao.PurchaseDao;
import com.yuanxiang.gulimall.ware.entity.PurchaseEntity;
import com.yuanxiang.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;
    @Autowired
    WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceiveList(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchaseItem(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            //新建一个采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        //TODO 确定采购单状态是0还是1
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(i);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

    /**
     * SELECT id,amount,ware_id,create_time,phone,assignee_name,update_time,priority,assignee_id,status FROM wms_purchase WHERE id=?
     * UPDATE wms_purchase SET phone=?, assignee_name=?, update_time=?, priority=?, assignee_id=?, status=? WHERE id=?
     * UPDATE wms_purchase_detail SET status=? WHERE id=?
     * 基本都是新建一个实体类--有ID信息和要修改的信息  然后对其进行修改
     * @param ids
     */
    @Override
    public void received(List<Long> ids) {
        //1、确定当前采购单是已分配的状态
        List<PurchaseEntity> collect=ids.stream().map(i -> {
            PurchaseEntity purchaseEntity = this.getById(i);
            return purchaseEntity;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() || item.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item->{
           item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
           item.setUpdateTime(new Date());
           return item;
        }).collect(Collectors.toList());
        //2、改变采购单的状态
        this.updateBatchById(collect);
        //3、改变采购项的状态
        collect.forEach(item->{
            List<PurchaseDetailEntity> entities=purchaseDetailService.listDetailBypurchaseId(item.getId());
            List<PurchaseDetailEntity> collect1 = entities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());//此处只更新status，只对此部分字段写入
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect1);
        });
    }

    /**
     * 请求参数{
     *    id: 123,//采购单id
     *    items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     * }
     * @param purchaseDoneVo
     */
    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {

        Long id = purchaseDoneVo.getId();
        //2、改变采购项的状态
        Boolean flag=true;
        List<PurchaseItemDoneVo> purchaseItemDoneVos = purchaseDoneVo.getItems();

        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for(PurchaseItemDoneVo item:purchaseItemDoneVos){
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if(item.getStatus()==WareConstant.PurchaseDetailStatusEnum.HASERRO.getCode()){
                flag=false;
                detailEntity.setStatus(item.getStatus());
            }else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //3、入库
                PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(purchaseDetailEntity.getSkuId(), purchaseDetailEntity.getWareId(), purchaseDetailEntity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        purchaseDetailService.updateBatchById(updates);
        //1、改变采购单的状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISH.getCode() :WareConstant.PurchaseStatusEnum.HASERRO.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

}