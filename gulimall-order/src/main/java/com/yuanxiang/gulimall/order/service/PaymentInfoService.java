package com.yuanxiang.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.gulimall.order.entity.PaymentInfoEntity;

import java.util.Map;

/**
 * 支付信息表
 *
 * @author yuanxiang
 * @email 1045703639@qq.com
 * @date 2021-03-16 16:42:54
 */
public interface PaymentInfoService extends IService<PaymentInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

