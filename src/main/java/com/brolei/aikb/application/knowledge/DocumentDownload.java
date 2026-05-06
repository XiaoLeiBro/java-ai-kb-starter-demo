package com.brolei.aikb.application.knowledge;

/** 可下载的知识库原始文档. */
public record DocumentDownload(String filename, String contentType, byte[] content) {}
