package com.yuanxiang.gulimall.seckill.to;

import com.yuanxiang.gulimall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecKillSkuRedisTo {
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;


    //1、秒杀的随机码：
    private String randomCode;


    //2、秒杀的开始时间
    private Long startTime;

    //2、秒杀的结束时间
    private Long endTime;

    private SkuInfoVo skuInfoVo;
}
