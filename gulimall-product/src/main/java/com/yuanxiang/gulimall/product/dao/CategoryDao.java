package com.yuanxiang.gulimall.product.dao;

import com.yuanxiang.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author yuanxiang
 * @email sunlightcs@gmail.com
 * @date 2021-03-16 12:06:37
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
