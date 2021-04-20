package com.yuanxiang.gulimall.coupon.dao;

import com.yuanxiang.gulimall.coupon.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 * 
 * @author yuanxiang
 * @email 1045703639@qq.com
 * @date 2021-03-16 16:14:33
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {
	
}
