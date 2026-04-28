package com.brolei.aikb.infrastructure.persistence.chat.invocation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** LLM 调用记录 MyBatis-Plus Mapper. */
@Mapper
public interface InvocationLogMapper extends BaseMapper<InvocationLogPo> {}
