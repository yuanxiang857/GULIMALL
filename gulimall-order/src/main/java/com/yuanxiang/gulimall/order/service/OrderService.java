package com.yuanxiang.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanxiang.common.to.mq.OrderTo;
import com.yuanxiang.common.to.mq.SeckillOrderTo;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.gulimall.order.entity.OrderEntity;
import com.yuanxiang.gulimall.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author yuanxiang
 * @email 1045703639@qq.com
 * @date 2021-03-16 16:42:54
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity orderEntity);


    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItems(Map<String, Object> params);

    String handlePayResult(PayAsyncVo vo);

    void closeSeckillOrder(SeckillOrderTo orderTo);
}

