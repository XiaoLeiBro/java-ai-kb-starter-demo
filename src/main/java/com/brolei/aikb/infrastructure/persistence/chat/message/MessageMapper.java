package com.brolei.aikb.infrastructure.persistence.chat.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 对话消息 MyBatis-Plus Mapper. */
@Mapper
public interface MessageMapper extends BaseMapper<MessagePo> {}
