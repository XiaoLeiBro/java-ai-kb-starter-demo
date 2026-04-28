package com.brolei.aikb.infrastructure.persistence.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brolei.aikb.infrastructure.persistence.user.po.UserPo;
import org.apache.ibatis.annotations.Mapper;

/** 用于用户持久化的 MyBatis-Plus Mapper 接口. */
@Mapper
public interface UserMapper extends BaseMapper<UserPo> {}
