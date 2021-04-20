package com.yuanxiang.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {
    //收货地址
    @Getter
    @Setter
    List<MemberAddressVo> address;
    //所有选中的购物项
    @Getter
    @Setter
    List<OrderItemVo> items;
    //发票记录
    //优惠卷信息
    @Getter
    @Setter
    Integer integration;

    @Getter
    @Setter
    Map<Long, Boolean> stocks;

    @Getter
    @Setter
    String orderToken;//防重令牌

    public Integer getCount() {
        Integer i = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        return i;
    }

//    BigDecimal total;//订单的总额

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    BigDecimal payPrice;//应该付的价格

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
