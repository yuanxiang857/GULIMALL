package com.yuanxiang.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yuanxiang.common.utils.HttpUtils;
import com.yuanxiang.gulimall.member.dao.MemberLevelDao;
import com.yuanxiang.gulimall.member.entity.MemberLevelEntity;
import com.yuanxiang.gulimall.member.exception.PhoneExistException;
import com.yuanxiang.gulimall.member.exception.UserNameExistException;
import com.yuanxiang.gulimall.member.vo.MemberLoginVo;
import com.yuanxiang.gulimall.member.vo.MemberRegistVo;
import com.yuanxiang.gulimall.member.vo.SocialUserVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.common.utils.Query;

import com.yuanxiang.gulimall.member.dao.MemberDao;
import com.yuanxiang.gulimall.member.entity.MemberEntity;
import com.yuanxiang.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = new MemberEntity();
        //设置默认等级
        MemberLevelEntity levelEntity = memberDao.getLevel();
        entity.setLevelId(levelEntity.getId());

        //抛异常机制
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());
        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());
        entity.setNickname(vo.getUserName());

        //密码要进行加密存储
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);

        memberDao.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        MemberDao memberDao = this.baseMapper;
        Integer member = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (member > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException {
        MemberDao memberDao = this.baseMapper;
        Integer member = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (member > 0) {
            throw new UserNameExistException();
        }
    }

    //登录操作
    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String userName = vo.getLoginacct();
        String password = vo.getPassword();

        MemberDao memberDao = this.baseMapper;
        //验证账户是否存在
        MemberEntity entity = new MemberEntity();
        entity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username", userName).or().eq("mobile", userName));
        if (entity == null) {
            return null;
        } else {
            //验证密码是否正确
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, entity.getPassword());
            if (matches) {
                return entity;
            } else {
                return null;
            }
        }
    }

    //社交登录
    @Override
    public MemberEntity login(SocialUserVo vo) throws Exception {
        String uid = vo.getUid();
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null) {
            //如果此社交账号已经登陆过，则只是执行更新操作，更新access_token
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccess_token(vo.getAccess_token());
            update.setExpires_in(vo.getExpires_in());
            memberDao.updateById(update);

            //把返回的用户也要进行更新
            memberEntity.setAccess_token(vo.getAccess_token());
            memberEntity.setExpires_in(vo.getExpires_in());
            return memberEntity;
        } else {
            //如果此社交账号没有登陆过
            MemberEntity register = new MemberEntity();
            try {
                //从社交平台获得个人信息
                Map<String, String> query = new HashMap<>();
                query.put("access_token",vo.getAccess_token());
                query.put("uid", vo.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    //查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    //获得昵称
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");

                    register.setNickname(name);
                    register.setGender("m".equals(gender) ? 1 : 0);
                }
            } catch (Exception e) {
            }
            register.setSocial_uid(vo.getUid());
            register.setExpires_in(vo.getExpires_in());
            register.setAccess_token(vo.getAccess_token());
            memberDao.insert(register);
            return register;
        }
    }

}