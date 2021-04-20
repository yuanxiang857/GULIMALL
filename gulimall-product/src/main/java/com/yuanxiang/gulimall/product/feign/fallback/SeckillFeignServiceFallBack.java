package com.yuanxiang.gulimall.product.feign.fallback;

import com.yuanxiang.common.exception.BizCodeException;
import com.yuanxiang.common.utils.R;
import com.yuanxiang.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("熔断方法的调用");
        return R.error(BizCodeException.TO_MANY_REQUEST.getCode(), BizCodeException.TO_MANY_REQUEST.getMsg());
    }
}
