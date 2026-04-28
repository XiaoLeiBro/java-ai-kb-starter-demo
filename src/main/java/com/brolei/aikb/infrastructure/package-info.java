/**
 * 基础设施层（Infrastructure Layer）.
 *
 * <p>职责：提供领域层定义的接口的具体实现，隔离外部技术细节。
 *
 * <p>子包：
 *
 * <ul>
 *   <li>{@code persistence} MyBatis-Plus PO、Mapper、Repository 实现
 *   <li>{@code vector} pgvector / Milvus / ES kNN 向量存储实现
 *   <li>{@code llm} LangChain4j 适配、多模型 Provider 实现
 *   <li>{@code file} 文件存储（本地 / MinIO / OSS）
 *   <li>{@code config} Spring 配置、Bean 装配
 *   <li>{@code security} JWT、Spring Security 配置
 * </ul>
 *
 * <p>原则：
 *
 * <ul>
 *   <li>领域层定义接口，基础设施层实现（依赖倒置）
 *   <li>切换技术（如从 pgvector 换到 Milvus）应只影响这一层
 * </ul>
 */
package com.brolei.aikb.infrastructure;
