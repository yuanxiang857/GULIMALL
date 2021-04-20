package com.yuanxiang.gulimall.auth.feign;

import com.yuanxiang.common.utils.R;
import com.yuanxiang.gulimall.auth.vo.SocialUserVo;
import com.yuanxiang.gulimall.auth.vo.UserLoginVo;
import com.yuanxiang.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);
    @PostMapping("/member/member/oauth2/login")
    R oauthlogin(@RequestBody SocialUserVo vo) throws Exception;
}
