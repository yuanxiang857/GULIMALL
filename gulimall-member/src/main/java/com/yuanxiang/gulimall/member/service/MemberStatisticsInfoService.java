package com.yuanxiang.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.gulimall.member.entity.MemberStatisticsInfoEntity;

import java.util.Map;

/**
 * 会员统计信息
 *
 * @author yuanxiang
 * @email 1045703639@qq.com
 * @date 2021-03-16 16:31:45
 */
public interface MemberStatisticsInfoService extends IService<MemberStatisticsInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

