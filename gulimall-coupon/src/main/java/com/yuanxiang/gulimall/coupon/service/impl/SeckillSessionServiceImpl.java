package com.yuanxiang.gulimall.coupon.service.impl;

import com.yuanxiang.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.yuanxiang.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.common.utils.Query;

import com.yuanxiang.gulimall.coupon.dao.SeckillSessionDao;
import com.yuanxiang.gulimall.coupon.entity.SeckillSessionEntity;
import com.yuanxiang.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySku() {
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        //先判断
        if (list != null && list.size() > 0) {
            List<SeckillSessionEntity> promotion_id1 = list.stream().map(session -> {
                Long id = session.getId();
                //查出id对应的关联
                List<SeckillSkuRelationEntity> promotion_id = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_id", id));
                session.setRelationSkus(promotion_id);
                return session;
            }).collect(Collectors.toList());
            return promotion_id1;
        }
        return null;
    }

    public String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime of = LocalDateTime.of(now, min);
        String format = of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

    public String endTime() {
        LocalDate now = LocalDate.now();
        LocalTime max = LocalTime.MAX;
        LocalDateTime of = LocalDateTime.of(now, max);
        String format = of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

}