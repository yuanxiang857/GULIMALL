package com.yuanxiang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.gulimall.product.entity.AttrGroupEntity;
import com.yuanxiang.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.yuanxiang.gulimall.product.vo.SkuItemVo;
import com.yuanxiang.gulimall.product.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author yuanxiang
 * @email sunlightcs@gmail.com
 * @date 2021-03-16 12:06:37
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> parms, Long catelogId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatlogId(Long catelogId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

