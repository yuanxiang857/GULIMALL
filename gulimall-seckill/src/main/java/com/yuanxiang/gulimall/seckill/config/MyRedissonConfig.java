package com.yuanxiang.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {


    /**
     * 所有redisson的使用都是通过RedissonClient对象
     * @return
     * @throws IOException
     */
//    @Bean(destroyMethod="shutdown")
//    RedissonClient redisson() throws IOException {
//        Config config = new Config();
//        config.useSingleServer().setAddress("redis://8.136.201.243:6379");
//        RedissonClient redissonClient = Redisson.create(config);
//        return redissonClient;
//    }
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        //1、创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://8.136.201.243:6379");

        //2、根据Config创建出RedissonClient实例
        //Redis url should start with redis:// or rediss://
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
