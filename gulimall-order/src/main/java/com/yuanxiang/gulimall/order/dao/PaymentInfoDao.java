package com.yuanxiang.gulimall.order.dao;

import com.yuanxiang.gulimall.order.entity.PaymentInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付信息表
 * 
 * @author yuanxiang
 * @email 1045703639@qq.com
 * @date 2021-03-16 16:42:54
 */
@Mapper
public interface PaymentInfoDao extends BaseMapper<PaymentInfoEntity> {
	
}
