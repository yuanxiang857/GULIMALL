package com.yuanxiang.gulimall.seckill.service.imp;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.yuanxiang.common.to.mq.SeckillOrderTo;
import com.yuanxiang.common.utils.R;
import com.yuanxiang.common.vo.MemberRespVo;
import com.yuanxiang.gulimall.seckill.feign.CouponFeignService;
import com.yuanxiang.gulimall.seckill.feign.ProductFeignService;
import com.yuanxiang.gulimall.seckill.intercepter.LoginUserInterceptor;
import com.yuanxiang.gulimall.seckill.service.SeckillService;
import com.yuanxiang.gulimall.seckill.to.SecKillSkuRedisTo;
import com.yuanxiang.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.yuanxiang.gulimall.seckill.vo.SeckillSkuVo;
import com.yuanxiang.gulimall.seckill.vo.SkuInfoVo;
import jdk.nashorn.internal.ir.Block;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImp implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;

    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKUKILL_STOCK_PREFIX = "seckill:stock:";

    public void getLatest3DaysSku() {
        //上架最近三天需要秒杀的商品
        R latest3DaysSku = couponFeignService.getLatest3DaysSku();
        if (latest3DaysSku.getCode() == 0) {
            //上架商品
            List<SeckillSessionsWithSkus> data = latest3DaysSku.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //储存到redis
            //1、缓存活动信息
            saveSessionInfos(data);
            //2、缓存活动的关联商品的信息
            saveSessionSkuInfos(data);
        }
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> data) {
        if (data != null) {
            data.stream().forEach(session -> {
                //1、获得key
                long startTime = session.getStartTime().getTime();
                long endTime = session.getEndTime().getTime();
                String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
                //2、获得value
                if (!stringRedisTemplate.hasKey(key)) {
                    List<String> s = session.getRelationSkus().stream().map(item -> item.getPromotionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                    stringRedisTemplate.opsForList().leftPushAll(key, s);
                }
            });
        }
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> data) {
        if (data != null) {
            BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            data.stream().forEach(session -> {
                session.getRelationSkus().stream().forEach(item -> {
                    if (!operations.hasKey(item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString())) {
                        SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                        //1、sku的基本数据
                        R info = productFeignService.info(item.getSkuId());
//                if (info.getCode() == 0) {
                        SkuInfoVo data1 = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfoVo(data1);
//                }
                        //2、sku的秒杀信息
                        BeanUtils.copyProperties(item, redisTo);
                        //3、设置上当前商品的秒杀时间信息
                        redisTo.setStartTime(session.getStartTime().getTime());
                        redisTo.setEndTime(session.getEndTime().getTime());
                        //4、随机码 防止提前的恶意请求
                        String replace = UUID.randomUUID().toString().replace("-", "");
                        redisTo.setRandomCode(replace);
                        String s = JSON.toJSONString(redisTo);
                        operations.put(item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString(), s);
                        //5、分布式信号量-》扣除库存 大并发 限流
                        RSemaphore semaphore = redissonClient.getSemaphore(SKUKILL_STOCK_PREFIX + replace);
                        //信号量的大小为商品库存的数量.
                        semaphore.trySetPermits(item.getSeckillCount());
                    }
                });
            });
        }
    }

    public List<SecKillSkuRedisTo> blockHandler(BlockException e) {
        log.error("getCurrentSeckillSkuResource被限流了");
        return null;
    }

    /**
     * blockHandler 函数会在原方法被限流/降级/系统保护的时候调用，而fallback函数会针对所有类型的异常
     * @return
     */
    @SentinelResource(value = "getCurrentSeckillSkuResource",blockHandler = "blockHandler")
    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckKillSkus() {
        //1、确定当前时间段的场次信息
        Long time = new Date().getTime();
        try (Entry entry = SphU.entry("seckKillSkus")) {
            Set<String> keys = stringRedisTemplate.keys(SESSION_CACHE_PREFIX + "*");
            for (String key : keys) {
                String replace = key.replace(SESSION_CACHE_PREFIX, "");
                String[] s = replace.split("_");
                Long startTime = Long.parseLong(s[0]);
                Long endTime = Long.parseLong(s[1]);
                if (time >= startTime && time <= endTime) {
                    //2、获取这个秒杀场次需要的商品信息
                    List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> operations = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    List<String> objects = operations.multiGet(range);
                    if (objects != null) {
                        List<SecKillSkuRedisTo> collect = objects.stream().map(item -> {
                            SecKillSkuRedisTo redisTo = JSON.parseObject(item, SecKillSkuRedisTo.class);
                            return redisTo;
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
        } catch (BlockException e) {
            log.error("资源被限流",e.getMessage());
        }
        //2、确定哪个时间段的商品
        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> operations = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = operations.keys();
        if (keys != null && keys.size() > 0) {
            String reg = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(reg, key)) {
                    String s = operations.get(key);
                    SecKillSkuRedisTo redisTo = JSON.parseObject(s, SecKillSkuRedisTo.class);
                    //随机码
                    Long time = new Date().getTime();
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    if (time >= startTime && time <= endTime) {
                    } else {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        long s1 = System.currentTimeMillis();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        //1、获取当前商品的详细信息。
        BoundHashOperations<String, String, String> operations = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = operations.get(killId);
        if (StringUtils.isEmpty(s)) {
            return null;
        } else {
            SecKillSkuRedisTo redisTo = JSON.parseObject(s, SecKillSkuRedisTo.class);
            //2、验证时间的合法性
            Long time = new Date().getTime();
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            Long ttl = endTime - time;//活动剩余时间
            if (time >= startTime && time <= endTime) {
                //3、校验随机码和商品id
                String randomCode = redisTo.getRandomCode();
                String skuId = redisTo.getPromotionId() + "_" + redisTo.getSkuId();
                if (randomCode.equals(key) && killId.equals(skuId)) {
                    //4、校验购物数量是否合理
                    if (num <= redisTo.getSeckillLimit()) {
                        String redisKey = memberRespVo.getId() + "_" + skuId;
                        //5、验证这个人是否已经买过，幂等性。只要秒杀成功，就去占位
                        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            //占位成功
                            RSemaphore semaphore = redissonClient.getSemaphore(SKUKILL_STOCK_PREFIX + randomCode);
//                                semaphore.acquire(num);//阻塞
                            boolean b = semaphore.tryAcquire(num);
                            if (b) {
                                //快速下单，到现在只是快速发送mq消息
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                //订单详情
                                orderTo.setOrderSn(timeId);
                                orderTo.setSkuId(redisTo.getSkuId());
                                orderTo.setNum(num);
                                orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                orderTo.setMemberId(memberRespVo.getId());
                                orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                long s2 = System.currentTimeMillis();
                                log.info("耗时=", (s2 - s1));
                                return timeId;
                            }
                        } else {
                            return null;
                        }
                    }

                } else {
                    return null;
                }
            } else {
                return null;
            }
            return null;
        }
    }

}
