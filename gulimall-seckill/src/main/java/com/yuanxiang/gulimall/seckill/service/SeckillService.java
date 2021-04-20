package com.yuanxiang.gulimall.seckill.service;

import com.yuanxiang.gulimall.seckill.to.SecKillSkuRedisTo;

import java.util.List;

public interface SeckillService {
    void getLatest3DaysSku();

    List<SecKillSkuRedisTo> getCurrentSeckKillSkus();

    SecKillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
