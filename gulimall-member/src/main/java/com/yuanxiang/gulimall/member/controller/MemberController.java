package com.yuanxiang.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.yuanxiang.common.exception.BizCodeException;
import com.yuanxiang.gulimall.member.exception.PhoneExistException;
import com.yuanxiang.gulimall.member.exception.UserNameExistException;
import com.yuanxiang.gulimall.member.feign.CouponFeignService;
import com.yuanxiang.gulimall.member.vo.MemberLoginVo;
import com.yuanxiang.gulimall.member.vo.MemberRegistVo;
import com.yuanxiang.gulimall.member.vo.SocialUserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yuanxiang.gulimall.member.entity.MemberEntity;
import com.yuanxiang.gulimall.member.service.MemberService;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.common.utils.R;


/**
 * 会员
 *
 * @author yuanxiang
 * @email 1045703639@qq.com
 * @date 2021-03-16 16:31:46
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R membercoupons = couponFeignService.membercoupons();
        return R.ok().put("member", memberEntity).put("coupons", membercoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }

    //社交登录
    @PostMapping("/oauth2/login")
    public R oauthlogin(@RequestBody SocialUserVo vo) throws Exception {
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        }else {
            return R.error(BizCodeException.LOINGACCT_PASSWORD_UNVALID_EXCEPTION.getCode(), BizCodeException.LOINGACCT_PASSWORD_UNVALID_EXCEPTION.getMsg());
        }
    }

    //登录
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {

        MemberEntity entity=memberService.login(vo);
        if (entity != null) {
            return R.ok().setData(entity);
        }else {
            return R.error(BizCodeException.LOINGACCT_PASSWORD_UNVALID_EXCEPTION.getCode(), BizCodeException.LOINGACCT_PASSWORD_UNVALID_EXCEPTION.getMsg());
        }
    }

    //注册
    @PostMapping("/register")
    public R regist(@RequestBody MemberRegistVo vo) {
        try {
            memberService.regist(vo);
        } catch (UserNameExistException e) {
            return R.error(BizCodeException.USER_EXIST_EXCEPTION.getCode(), BizCodeException.USER_EXIST_EXCEPTION.getMsg());
        } catch (PhoneExistException e) {
            return R.error(BizCodeException.PHONE_EXIST_EXCEPTION.getCode(), BizCodeException.PHONE_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
