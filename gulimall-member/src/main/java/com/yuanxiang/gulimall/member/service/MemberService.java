package com.yuanxiang.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.gulimall.member.entity.MemberEntity;
import com.yuanxiang.gulimall.member.exception.PhoneExistException;
import com.yuanxiang.gulimall.member.exception.UserNameExistException;
import com.yuanxiang.gulimall.member.vo.MemberLoginVo;
import com.yuanxiang.gulimall.member.vo.MemberRegistVo;
import com.yuanxiang.gulimall.member.vo.SocialUserVo;

import java.util.Map;

/**
 * 会员
 *
 * @author yuanxiang
 * @email 1045703639@qq.com
 * @date 2021-03-16 16:31:46
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone)throws PhoneExistException;

    void checkUserNameUnique(String username)throws UserNameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUserVo vo) throws Exception;//方法的重载
}

