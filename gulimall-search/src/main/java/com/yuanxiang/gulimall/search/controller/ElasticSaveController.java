package com.yuanxiang.gulimall.search.controller;

import com.yuanxiang.common.exception.BizCodeException;
import com.yuanxiang.common.to.es.SkuEsModel;
import com.yuanxiang.common.utils.R;
import com.yuanxiang.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean b = false;
        try {
            b = productSaveService.productStatusUp(skuEsModels);
        } catch (Exception e) {
            log.error("elasticsearch商品上架错误:{}", e);
            return R.error(BizCodeException.PRODUCT_UP_EXCEPTION.getCode(), BizCodeException.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (!b) {
            return R.ok();
        }else {
            return R.error(BizCodeException.PRODUCT_UP_EXCEPTION.getCode(), BizCodeException.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}
