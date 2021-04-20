package com.yuanxiang.gulimall.seckill.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 定制RabbitTemplate--可靠抵达
 * 1、服务收到消息就回调
 * 2、消息正确抵达队列回调
 *  #开启发送端消息抵达队列确认
 *  spring.rabbitmq.publisher-returns=true
 *  #只要抵达队列，以异步方式优先回调
 *  spring.rabbitmq.template.mandatory=true
 *
 * 3、消费端确认（确保每个消息被正确消费，此时才可以broker删除这个消息）
 *      1、默认是自动确认的，只要消息收到，客户端就会自动确认，服务端就会移除这个消息。
 *          问题：我们收到很多消息，自动回复给服务器ack。只有一个消息处理成功，宕机了。发生消息丢失-->手动确认
 *      2、手动模式下。只要我们没有明确告诉MA，货物被签收。没有Ack
 *      消息就一直是unacked状态。即使consumer宕机。消息也不会丢失，会重新变成Ready
 *      3、如何签收
 *          channel.basicAck(deliveryTag, false);签收货物，业务成功完成就应该签收
 *          channel.basicNack(deliveryTag,false,true);拒签且重新入队
 *
 *
 */


//把消息转换方式换成json
@Configuration
public class MyRabbitConfig {

    /**
     * 使用JSON序列化机制 进行消息转换
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

//    @PostConstruct// MyRabbitConfig对象创建完成后，执行这个方法
//    public void initConfirmCallBack() {
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//            /**
//             *
//             * 1 做好消息的确认机制 手动ack
//             * 2  每一个发送的消息都在数据库中做好记录.定期将失败的消息再次发送.
//             *
//             *只要消息抵达broker ack则为true
//             * @param correlationData 当前消息的唯一关联数据（这个是消息的唯一id）
//             * @param ack   消息是否成功收到
//             * @param cause 失败的原因
//             */
//            @Override
//            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                System.out.println("confirm....correlationData["+correlationData+"]==>ack["+ack+"]==>cause["+cause+"]");
//            }
//        });
//        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
//            /**
//             * 只要消息没有投递到指定的队列，就触发这个失败回调
//             * @param message    投递失败的详细信息
//             * @param replyCode     回复的状态码
//             * @param replyText     回复的文本内容
//             * @param exchange      当时这个消息发送给哪个交换机
//             * @param routingKey    当时这个消息用哪个路由键
//             */
//            @Override
//            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//                System.out.println("1::"+message+"2::"+replyCode+"3::"+replyText+"4::"+exchange+"5::"+routingKey);
//            }
//        });
//    }

}
