package com.yuanxiang.gulimall.order;

import com.yuanxiang.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {


//	@Autowired
//	AmqpAdmin amqpAdmin;
//
//	@Autowired
//	RabbitTemplate rabbitTemplate;
//	@Test
//	public void testSendMessageTest() {
//
//		//如果发送的消息是对象，我们会使用序列化机制，将对象写出去。对象必须实现serialzable
//		OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
//		orderReturnReasonEntity.setId(1L);
//		orderReturnReasonEntity.setCreateTime(new Date());
//		orderReturnReasonEntity.setName("哈哈哈");
//		//发送消息
//		String msg="helloWorld";
//		rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderReturnReasonEntity,new CorrelationData(UUID.randomUUID().toString()));
//		log.info("消息发送完成{}",orderReturnReasonEntity);
//	}
//
//
//
//	@Test
//	public void creatExchange() {
//		//public DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
//		DirectExchange directExchange = new DirectExchange("hello-java-exchange",true,false);
//		amqpAdmin.declareExchange(directExchange);
//		log.info("exchange创建成功");
//	}
//	@Test
//	public void creatQueue() {
//		//public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
//		Queue queue = new Queue("hello-java-queue",true,false,false);
//		amqpAdmin.declareQueue(queue);
//	}
//	@Test
//	public void creatBinding() {
//		//public Binding(String destination, DestinationType destinationType, String exchange, String routingKey,
//		//			Map<String, Object> arguments)
//		//将exchange的指定的交换机和destination目的地进行绑定
//		Binding binding = new Binding("hello-java-queue",Binding.DestinationType.QUEUE,"hello-java-exchange","hello.java",null);
//		amqpAdmin.declareBinding(binding);
//	}

}
