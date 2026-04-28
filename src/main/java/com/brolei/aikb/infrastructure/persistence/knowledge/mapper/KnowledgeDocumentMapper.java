package com.brolei.aikb.infrastructure.persistence.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.KnowledgeDocumentPo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 知识文档 MyBatis-Plus Mapper 接口. */
@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocumentPo> {

  /** 按文档 ID 列表批量查询. */
  List<KnowledgeDocumentPo> findByDocIds(@Param("docIds") List<String> docIds);
}
