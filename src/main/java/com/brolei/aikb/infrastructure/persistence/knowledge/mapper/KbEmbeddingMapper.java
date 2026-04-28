package com.brolei.aikb.infrastructure.persistence.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.KbEmbeddingPo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 向量嵌入 MyBatis-Plus Mapper 接口. */
@Mapper
public interface KbEmbeddingMapper extends BaseMapper<KbEmbeddingPo> {

  /** 批量插入向量嵌入记录. */
  void insertBatch(List<KbEmbeddingPo> list);

  /** 按知识库 ID 进行向量相似度检索. */
  List<KbEmbeddingPo> searchByKbId(
      @Param("knowledgeBaseId") String knowledgeBaseId,
      @Param("embedding") float[] embedding,
      @Param("topK") int topK);
}
