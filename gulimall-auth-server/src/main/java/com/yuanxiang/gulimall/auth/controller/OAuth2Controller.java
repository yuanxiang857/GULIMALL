package com.yuanxiang.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yuanxiang.common.constant.AuthServerConstant;
import com.yuanxiang.common.utils.HttpUtils;
import com.yuanxiang.common.utils.R;
import com.yuanxiang.gulimall.auth.feign.MemberFeignService;
import com.yuanxiang.common.vo.MemberRespVo;
import com.yuanxiang.gulimall.auth.vo.SocialUserVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;



@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService feignService;
    @RequestMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> header = new HashMap<>();
        Map<String, String> query = new HashMap<>();

        Map<String, String> map = new HashMap<>();
        map.put("client_id", "3401093614");
        map.put("client_secret", "ecc5656473b29eefd98c2129b58b6205");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        //根据code换取accessToken。
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", header, query, map);
        //2、处理
        if (response.getStatusLine().getStatusCode() == 200) {
            String json = EntityUtils.toString(response.getEntity());
            SocialUserVo socialUserVo = JSON.parseObject(json, SocialUserVo.class);
            //调用远程的登录
            R r = feignService.oauthlogin(socialUserVo);
            if (r.getCode() == 0) {
                MemberRespVo data = r.getData("data", new TypeReference<MemberRespVo>() {
                });
                //TODO 1、默认发的令牌。作用域为当前域===需要解决自愈session共享问题。。目前是手动设置的
                //TODO 2、使用JSON的序列化方式来序列化对象数据到redis中
                session.setAttribute(AuthServerConstant.LOGIN_USER,data);
                //返回首页
                return "redirect:http://gulimall.com";
            }else {
            return "redirect:http://gulimall.com/login.html";
            }
        } else {
            return "redirect:http://gulimall.com/login.html";
        }
    }
}
