package com.yuanxiang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.yuanxiang.gulimall.product.vo.SkuItemSaleAttr;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author yuanxiang
 * @email sunlightcs@gmail.com
 * @date 2021-03-16 12:06:37
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemSaleAttr> getSaleAttrsBySpuId(Long spuId, long catalogId);

    List<String> getSkuSaleAttrValuesAsStringList(Long skuId);
}

