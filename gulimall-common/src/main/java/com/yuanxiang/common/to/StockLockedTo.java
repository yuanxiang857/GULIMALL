package com.yuanxiang.common.to;


import lombok.Data;


@Data
public class StockLockedTo {

    private Long id;//库存工作单
    private StockDetailTo detail;//工作单详情的ID


}
