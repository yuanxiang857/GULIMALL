package com.yuanxiang.gulimall.order.vo;

import com.yuanxiang.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;//状态码

}
