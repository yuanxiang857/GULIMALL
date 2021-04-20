package com.yuanxiang.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.gulimall.coupon.entity.MemberPriceEntity;

import java.util.Map;

/**
 * 商品会员价格
 *
 * @author yuanxiang
 * @email 1045703639@qq.com
 * @date 2021-03-16 16:14:33
 */
public interface MemberPriceService extends IService<MemberPriceEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

