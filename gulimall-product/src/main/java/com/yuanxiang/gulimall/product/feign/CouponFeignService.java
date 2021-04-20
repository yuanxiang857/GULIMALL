package com.yuanxiang.gulimall.product.feign;

import com.yuanxiang.common.to.SkuReductionTo;
import com.yuanxiang.common.to.SpuBoundsTo;
import com.yuanxiang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /**
     * 远程调用并且传了一个对象
     *  1、requestbody把这个对象转成了json
     *  2、找到gulimall-coupon服务，给、coupon、spubounds、save发送请求
     *      将上一步转的json放在请求体位置，发送请求；
     *  3、对方服务收到请求。请求体里面会受到json数据
     *      @requestBody；将请求体里卖弄的json转化为spuBoundsEntity
     *  只要json数据模型是兼容的。双方服务无需使用同一个to
     * @param spuBoundsTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
