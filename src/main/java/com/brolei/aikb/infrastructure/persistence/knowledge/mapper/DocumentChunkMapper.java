package com.brolei.aikb.infrastructure.persistence.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brolei.aikb.infrastructure.persistence.knowledge.po.DocumentChunkPo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 文档切片 MyBatis-Plus Mapper 接口. */
@Mapper
public interface DocumentChunkMapper extends BaseMapper<DocumentChunkPo> {

  /** 按切片 ID 列表批量查询. */
  List<DocumentChunkPo> findByChunkIds(@Param("chunkIds") List<String> chunkIds);
}
