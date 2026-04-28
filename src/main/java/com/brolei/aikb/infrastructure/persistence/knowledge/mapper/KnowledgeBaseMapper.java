package com.brolei.aikb.infrastructure.persistence.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.KnowledgeBasePo;
import org.apache.ibatis.annotations.Mapper;

/** 知识库 MyBatis-Plus Mapper 接口. */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBasePo> {}
