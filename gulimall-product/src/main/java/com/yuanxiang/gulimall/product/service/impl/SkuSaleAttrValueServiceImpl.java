package com.yuanxiang.gulimall.product.service.impl;

import com.yuanxiang.gulimall.product.vo.SkuItemSaleAttr;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.common.utils.Query;

import com.yuanxiang.gulimall.product.dao.SkuSaleAttrValueDao;
import com.yuanxiang.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.yuanxiang.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttr> getSaleAttrsBySpuId(Long spuId, long catalogId) {
        SkuSaleAttrValueDao dao = this.baseMapper;
        List<SkuItemSaleAttr> attrs=dao.getSaleAttrVos(spuId);
        return attrs;
    }

    @Override
    public List<String> getSkuSaleAttrValuesAsStringList(Long skuId) {
        SkuSaleAttrValueDao dao = this.baseMapper;
        return dao.getSkuSaleAttr(skuId);
    }

}