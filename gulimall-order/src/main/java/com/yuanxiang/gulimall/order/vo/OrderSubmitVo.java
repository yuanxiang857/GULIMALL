package com.yuanxiang.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;

    //无需提交需要购买的商品，去购物车再获取一次
    //优惠，发票

    private String orderToken;//防重令牌
    private BigDecimal payPrice;

    //用户相关信息直接去session去取
    //订单备注
    private String note;
}
