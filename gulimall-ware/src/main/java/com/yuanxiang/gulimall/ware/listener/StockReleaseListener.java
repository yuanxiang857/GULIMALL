package com.yuanxiang.gulimall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.yuanxiang.common.to.StockDetailTo;
import com.yuanxiang.common.to.StockLockedTo;
import com.yuanxiang.common.to.mq.OrderTo;
import com.yuanxiang.common.utils.R;
import com.yuanxiang.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.yuanxiang.gulimall.ware.entity.WareOrderTaskEntity;
import com.yuanxiang.gulimall.ware.service.WareSkuService;
import com.yuanxiang.gulimall.ware.vo.OrderVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockerRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁信息....");
        try {
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            //消息拒绝之后重新返回队列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

    @RabbitHandler
    public void listener1(OrderTo orderTo, Channel channel, Message message)throws IOException {
        System.out.println("订单关闭准备解锁库存....");
        try {
            wareSkuService.unlockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
