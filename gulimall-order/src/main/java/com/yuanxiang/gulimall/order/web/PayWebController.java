package com.yuanxiang.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.yuanxiang.gulimall.order.config.AlipayTemplate;
import com.yuanxiang.gulimall.order.service.OrderService;
import com.yuanxiang.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayWebController {

    @Autowired
    AlipayTemplate alipayTemplate;

    /**
     * 让支付页让浏览器展示
     * 支付成功以后，我们要跳到订单列表页
     */
    @Autowired
    OrderService orderService;
    @GetMapping(value = "/aliPayOrder",produces = "text/html")
    @ResponseBody
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo=orderService.getOrderPay(orderSn);
        //返回的是一个页面，将此页面直接交给浏览器即可。
        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);
        return pay;
    }
}
