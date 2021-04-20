package com.yuanxiang.gulimall.seckill.scheduled;

import com.yuanxiang.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * 秒杀商品的定时上架;
 * 每天晚上3点。上架最近三天需要上架的商品
 */

@Slf4j
@Component
public class SeckillSkuScheduled {
    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock = "seckill:upload:lock";

    //TODO 进行幂等性处理
    @Scheduled(cron = "1 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        log.info("上架");
        //分布式锁。锁的业务执行完成，状态已经更新完成，释放锁以后。其他人获取到最新的状态
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.getLatest3DaysSku();
        }finally {
            lock.unlock();
        }

    }
}
