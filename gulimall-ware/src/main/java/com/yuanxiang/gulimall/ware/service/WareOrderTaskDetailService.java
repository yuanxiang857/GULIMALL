package com.yuanxiang.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.gulimall.ware.entity.WareOrderTaskDetailEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author yuanxiang
 * @email 1045703639@qq.com
 * @date 2021-03-16 16:48:56
 */
public interface WareOrderTaskDetailService extends IService<WareOrderTaskDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

