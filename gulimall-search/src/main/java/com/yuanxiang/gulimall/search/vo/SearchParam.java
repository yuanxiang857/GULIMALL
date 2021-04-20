package com.yuanxiang.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递过来的参数
 */
@Data
public class SearchParam {
    private String keyword;
    private Long catalog3Id;
    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hostScore_asc/desc
     */
    private String sort;
    /**
     * 好多的过滤条件
     * hasStock,skuPrice,brandId,catalog3Id,attrs
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     *
     */
    private Integer hasStock=1;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;

    private Integer pageNum=1;
    private String _queryString;//原生的所有的查询条件

}
