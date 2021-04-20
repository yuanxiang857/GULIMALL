package com.yuanxiang.gulimall.order.service.impl;

import com.rabbitmq.client.Channel;
import com.yuanxiang.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.common.utils.Query;

import com.yuanxiang.gulimall.order.dao.OrderItemDao;
import com.yuanxiang.gulimall.order.entity.OrderItemEntity;
import com.yuanxiang.gulimall.order.service.OrderItemService;


@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * queues 声明需要监听的所有队列
     * 参数写一些类型
     * 1、Message message 原生消息=头+体
     * 2、T<发送的消息的类型> OrderReturnReasonEntity content
     * 3、Channel channel:当前传输的通道
     * <p>
     * Queue：可以很多人都来监听。只要收到消息 队列删除消息。而且只能由一个人收到此消息
     * 1、分布式情况下：订单服务启动多个
     * 1、订单启动多个。同一个消息。只能有一个客户端收到
     * 2、只有一个消息完全处理完，方法运行结束，才会继续接收消息
     * 2、
     *
     * @param message
     */
//    @RabbitListener(queues = {"hello-java-queue"})
//    public void receiveMessage(Message message, OrderReturnReasonEntity content, Channel channel) {
//        System.out.println("接收到的消息内容：" + message + "===>" + content);
//        //Body:'{"id":1,"name":"哈哈哈","sort":null,"status":null,"createTime":1618152933535}'
//        byte[] boby = message.getBody();
//        MessageProperties properties = message.getMessageProperties();
//        System.out.println("消息处理完成" + content.getName());
//        //channel内按顺序自增
//        long deliveryTag = message.getMessageProperties().getDeliveryTag();
//        System.out.println("deliveryTag==>" + deliveryTag);
//        //签收货物，非批量模式
//        try {
//            if (deliveryTag % 2 == 0) {
//
//                channel.basicAck(deliveryTag, false);
//                System.out.println("签收了货物" + deliveryTag);
//            } else {
////                channel.basicAck(deliveryTag);
//                //true 重新入队
//                channel.basicNack(deliveryTag,false,true);
//                System.out.println("没有签收货物"+deliveryTag);
//            }
//        } catch (Exception e) {
//        }
//    }

}