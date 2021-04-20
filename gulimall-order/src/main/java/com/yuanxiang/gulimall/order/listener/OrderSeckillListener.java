package com.yuanxiang.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.yuanxiang.common.to.mq.SeckillOrderTo;
import com.yuanxiang.gulimall.order.entity.OrderEntity;
import com.yuanxiang.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo orderTo, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息:准备关闭订单" + orderTo.getOrderSn());
        try {
            log.info("准备创建秒杀单的详细信息");
            orderService.closeSeckillOrder(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


}
