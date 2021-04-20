package com.yuanxiang.gulimall.search.controller;

import com.yuanxiang.gulimall.search.service.MallSearchService;
import com.yuanxiang.gulimall.search.vo.SearchParam;
import com.yuanxiang.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;


@Controller
public class SearchController {

    /**
     * 将所有请求封装成一个对象.
     */
    @Autowired
    MallSearchService mallSearchService;
    @GetMapping("/list.html")
    public String list(SearchParam searchParam, Model model, HttpServletRequest request) {
        searchParam.set_queryString(request.getQueryString());
        //根据传递来的页面的查询参数，去es中检索商品
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
