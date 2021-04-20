package com.yuanxiang.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用rabbitMQ
 * 	1、引入依赖 RabbitAutoConfiguration就会生效
 * 	2、RabbitTemplate、AmqpAdmin、rabbitConnectionFactory、RabbitMessagingTemplate
 * 		所有的属性在RabbitProperties绑定
 * 	3、@EnableRabbit: @Enablexxx
 * 	4、配置文件中配置	spring.rabbitmq.host=8.136.201.243
 *					 spring.rabbitmq.port=5672
 * 					spring.rabbitmq.virtual-host=/
 * 	5、监听消息@RabbitListener 必须的有@EnableRabbit --像其他的上述的操作则不需要
 * 			@RabbitListener 类和方法（监听哪些队列）
 * 			@RabbitHandler 方法(重载区分不同的消息)
 *
 *
 * 	1)、Amqp进行创建
 */

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallOrderApplication.class, args);
	}

}
