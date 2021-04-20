package com.yuanxiang.gulimall.seckill.controller;

import com.yuanxiang.common.utils.R;
import com.yuanxiang.gulimall.seckill.service.SeckillService;
import com.yuanxiang.gulimall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SecKillController {

    //TODO 上架秒杀商品的时候，每一个数据都有过期时间
    //TODO 秒杀后续的流程，简化了收货地址等信息
    @Autowired
    SeckillService seckillService;
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSku() {
        //返回当前时间可以参与的秒杀商品信息
        List<SecKillSkuRedisTo> redisTos= seckillService.getCurrentSeckKillSkus();
        return R.ok().setData(redisTos);
    }
    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedisTo redisTo=seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(redisTo);
    }

    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId, @RequestParam("key") String key, @RequestParam("num") Integer num,Model model) {
        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }


}
