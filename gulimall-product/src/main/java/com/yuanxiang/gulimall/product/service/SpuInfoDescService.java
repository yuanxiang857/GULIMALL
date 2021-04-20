package com.yuanxiang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.gulimall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author yuanxiang
 * @email sunlightcs@gmail.com
 * @date 2021-03-16 12:06:37
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfoDesc(SpuInfoDescEntity spuInfoDescEntity);
}

