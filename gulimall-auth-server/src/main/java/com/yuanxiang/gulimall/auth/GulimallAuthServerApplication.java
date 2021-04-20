package com.yuanxiang.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 * 核心原理
 * 	1)、@EnableRedisHttpSession导入RedisHttpSessionConfiguration
 * 		1、给容器中增加了一个组件
 * 				SessionRepository=>>RedisOperationsSessionRepository：redis来操作session。session的增删改查封装类
 * 		2、springSessionRepositoryFilter--》Filter：session存储过滤器，每个请求都必须经过filter
 * 				1、创建的时候，就自动从容器中获取到了SessionRepository
 * 				2、原始的request、response都被包装-->SessionRepositoryRequestWrapper、SessionRepositoryResponseWrapper
 * 				3、以后获取session。request.getSession();
 * 				-->变成了
 * 				4、wrappedRequest.getSession();--->SessionRepository中获取得到。
 * 		装饰者模式:
 * 		自动续期
 */

@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallAuthServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallAuthServerApplication.class, args);
	}

}
