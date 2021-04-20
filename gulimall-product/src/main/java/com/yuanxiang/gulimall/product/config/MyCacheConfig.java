package com.yuanxiang.gulimall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * 整合springcache简化开发缓存
 * 如果想要改变默认的ttl(Time to Live)时间，所需要改变的
 * 原理：
 *  CacheAutoConfiguration->RedisCacheConfiguration->自动配置了RedisCacheManager->初始化所有的缓存->每个缓存决定使用什么配置
 *  ->如果redisCacheConfiguration有就用已经有的，没有就用默认配置。
 *  ->想改缓存的配置，只需要给容器中放一个RedisCacheConfiguration即可
 *  ->就会应用到当前RedisCacheManager管理的所有缓存中
 */
//此处修改完后  ttl的设置又恢复成为了默认的了

/**
 * 为了使用源码中的properties属性
 * 1、注册Bean 2、将properties放到形参中
 * @ConfigurationProperties(prefix = "spring.cache")
 * public class CacheProperties {
 */

@EnableConfigurationProperties(CacheProperties.class)
@Configuration
@EnableCaching
public class MyCacheConfig{
    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        CacheProperties.Redis redisProperties = cacheProperties.getRedis();

        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }
}
