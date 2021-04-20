package com.yuanxiang.gulimall.search.vo;

import com.yuanxiang.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {
    //查询到的所有商品信息
    private List<SkuEsModel> products;
    private Integer pageNum;
    private Long total;
    private Integer totalPages;

    private List<BrandVo> brands;//当前查询的结果，所有涉及的品牌
    private List<CatalogVo> catalogs;//当前查询的结果，所有涉及到的所有分类
    private List<AttrVo> attrs;//所有涉及到的属性

    //==========以上是返回页面的信息============
    //面包屑导航数据
    private List<NavVo> navs=new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();
    @Data
    public static class  NavVo{
        private String navName;
        private String navValue;
        private String link;
    }
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrVal;
    }

}
