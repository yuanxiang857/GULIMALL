package com.yuanxiang.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.common.utils.Query;

import com.yuanxiang.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.yuanxiang.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.yuanxiang.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.util.StringUtils;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSkuRelationEntity> seckillSkuRelationEntityQueryWrapper = new QueryWrapper<SeckillSkuRelationEntity>();

        String promotionSessionId = (String)params.get("promotionSessionId");
        if (!StringUtils.isEmpty(promotionSessionId)) {
            seckillSkuRelationEntityQueryWrapper.eq("promotion_session_id",promotionSessionId);
        }
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                seckillSkuRelationEntityQueryWrapper
        );

        return new PageUtils(page);
    }

}