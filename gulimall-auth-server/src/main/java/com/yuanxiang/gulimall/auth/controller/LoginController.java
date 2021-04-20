package com.yuanxiang.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.yuanxiang.common.constant.AuthServerConstant;
import com.yuanxiang.common.utils.R;
import com.yuanxiang.common.vo.MemberRespVo;
import com.yuanxiang.gulimall.auth.feign.MemberFeignService;
import com.yuanxiang.gulimall.auth.vo.UserLoginVo;
import com.yuanxiang.gulimall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 发送一个请求直接跳转到一个界面
     * spring viewcontroller 请求直接跳转页面
     *
     * @return
     */
    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null) {
            return "login";
        }else {
            return "redirect:http://gulimall.com";
        }

    }

    //
//    @GetMapping("/reg.html")
//    public String regPage() {
//        return "reg";
//    }
    //RedirectAttributes 重定向的同时也可以获取数据
    //TODO 分布式下的session问题
    @PostMapping("/register")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            //校验
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            /**
             * 用户注册->/register[post]->转发/reg.html(路径映射默认为get方式)
             */
            return "redirect:http://auth.gulimall.com/reg.html";
//            return "reg";
        }
        //真正的注册
        //TODO  1、校验验证码--
        R r = memberFeignService.regist(vo);
        if (r.getCode() == 0) {
            //成功
            return "redirect:http://auth.gulimall.com/login.html";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getData(new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            MemberRespVo data = login.getData("data", new TypeReference<MemberRespVo>() {
            });
            //成功后放到data中
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
