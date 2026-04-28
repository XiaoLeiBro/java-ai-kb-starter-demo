-- PostgreSQL 初始化脚本
-- 启用 pgvector 扩展，用于向量相似度检索

CREATE EXTENSION IF NOT EXISTS vector;

-- 注意：表结构由 Spring Boot 应用通过 Flyway 迁移脚本创建
-- 这里仅预先启用扩展和准备基础环境
