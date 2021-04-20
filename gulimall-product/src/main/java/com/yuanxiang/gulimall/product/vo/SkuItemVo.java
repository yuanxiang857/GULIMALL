package com.yuanxiang.gulimall.product.vo;

import com.yuanxiang.gulimall.product.entity.SkuImagesEntity;
import com.yuanxiang.gulimall.product.entity.SkuInfoEntity;
import com.yuanxiang.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    //1、sku基本属性
    SkuInfoEntity info;
    boolean hasStock=true;
    //2、sku图片信息
    List<SkuImagesEntity> images;
    //3、spu销售属性组合
    List<SkuItemSaleAttr> saleAttr;
    //4、spu介绍
    SpuInfoDescEntity desp;
    //5、spu规格参数
    List<SpuItemAttrGroupVo> groupAttrs;

    //6、秒杀信息
    SeckillInfoVo seckillInfoVo;


}
