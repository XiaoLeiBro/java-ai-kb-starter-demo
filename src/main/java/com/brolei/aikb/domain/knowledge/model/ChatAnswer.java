package com.brolei.aikb.domain.knowledge.model;

import java.util.List;

/**
 * 聊天回答值对象.
 *
 * <p>包含 LLM 生成的回答文本以及作为参考依据的检索切片列表。
 */
public record ChatAnswer(String answer, List<RetrievedChunk> references) {}
