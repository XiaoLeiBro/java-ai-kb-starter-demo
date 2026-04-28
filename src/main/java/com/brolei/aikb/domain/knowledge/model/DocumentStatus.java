package com.brolei.aikb.domain.knowledge.model;

/** 文档索引状态. */
public enum DocumentStatus {

  /** 已上传，尚未开始索引. */
  UPLOADED,

  /** 正在索引切片与向量化. */
  INDEXING,

  /** 索引完成，可用于检索. */
  READY,

  /** 索引失败，需人工处理. */
  FAILED
}
