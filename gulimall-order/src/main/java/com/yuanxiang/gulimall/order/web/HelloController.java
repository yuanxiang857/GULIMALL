package com.yuanxiang.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HelloController {
    /**
     * 订单服务的页面跳转
     * @param page
     * @return
     */
    @GetMapping("/{page}.html")
    public String listPages(@PathVariable("page") String page) {
        return page;
    }

}
