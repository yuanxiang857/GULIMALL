package com.yuanxiang.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1、整合sentinel
 * 	1、导入依赖
 * 	2、下载sentinel的控制台（类似nacos
 * 	3、配置sentinel控制台地址信息。
 * 	4、在控制台跳转参数。【默认所有的流控设置保存在内存中，重启生效
 *
 *2、每个微服务都导入actuator；并配合management.endpoints.web.exposure.include=*
 *3、自定义sentinel流控返回数据
 *4、使用sentienl来保护feign远程调用：熔断.
 * 	1、调用方的熔断保护：
 			feign.sentinel.enabled=true
 	2、调用方手动指定远程服务的降级策略。远程服务被降级处理，默认触发我们的熔断回调
 	3、超大浏览的时候，必须牺牲一些远程服务。在服务的提供方（远程服务）指定降级策略。
 	提供方是在运行，但是不运行自己的业务逻辑。返回的是默认的熔断数据（限流的数据）
 *5、自定义受保护的资源
 * 		1、try (Entry entry = SphU.entry("seckKillSkus")) {
 *         } catch (BlockException e) {
 *             log.error("资源被限流",e.getMessage());
 *         }
 *		2、blockHandler 函数会在原方法被限流/降级/系统保护的时候调用，而fallback函数会针对所有类型的异常
 *		要配置被限流后的默认返回。
 */


@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallSeckillApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallSeckillApplication.class, args);
	}

}
