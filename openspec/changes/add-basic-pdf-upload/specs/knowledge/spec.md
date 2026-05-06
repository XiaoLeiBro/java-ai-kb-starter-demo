# Knowledge RAG 规格变更

## MODIFIED Requirements

### Requirement: 上传文档

系统 SHALL 允许用户向自己拥有的知识库上传 `.md`、`.txt` 或文本型 `.pdf` 文件，经过文本提取、文本切分、Embedding 和 pgvector 存储处理。

#### Scenario: 上传文本型 PDF 文档

- **假设** 用户已登录，且拥有指定的知识库
- **当** 用户请求 `POST /api/v1/knowledge-bases/{knowledgeBaseId}/documents`，上传包含可提取文本的 `.pdf` 文件
- **那么** 系统保存原始 PDF 文件
- **并且** 系统使用 PDF 文本提取结果进行切分、Embedding 和向量存储
- **并且** 返回 HTTP 200 和文档 ID、文件名、状态、切片数

#### Scenario: 上传空文本 PDF

- **假设** 用户已登录，且拥有指定的知识库
- **当** 用户上传扫描件 PDF、空白 PDF 或其他无法提取文本的 PDF
- **那么** 系统返回 HTTP 400
- **并且** 错误信息提示当前仅支持文本型 PDF，不支持扫描件 OCR

#### Scenario: 上传加密或损坏 PDF

- **假设** 用户已登录，且拥有指定的知识库
- **当** 用户上传加密 PDF 或损坏 PDF
- **那么** 系统返回 HTTP 400
- **并且** 错误信息不暴露底层解析堆栈
