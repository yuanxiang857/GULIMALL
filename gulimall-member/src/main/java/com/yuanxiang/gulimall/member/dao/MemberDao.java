package com.yuanxiang.gulimall.member.dao;

import com.yuanxiang.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuanxiang.gulimall.member.entity.MemberLevelEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author yuanxiang
 * @email 1045703639@qq.com
 * @date 2021-03-16 16:31:46
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

    MemberLevelEntity getLevel();
}
