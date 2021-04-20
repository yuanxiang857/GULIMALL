package com.yuanxiang.gulimall.product.dao;

import com.yuanxiang.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuanxiang.gulimall.product.vo.SkuItemSaleAttr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author yuanxiang
 * @email sunlightcs@gmail.com
 * @date 2021-03-16 12:06:37
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttr> getSaleAttrVos(@Param("spuId") Long spuId);

    List<String> getSkuSaleAttr(@Param("skuId") Long skuId);
}
