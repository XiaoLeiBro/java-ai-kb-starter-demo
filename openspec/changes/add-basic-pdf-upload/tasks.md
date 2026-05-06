## 1. 规格与边界

- [x] 1.1 新增 `add-basic-pdf-upload` OpenSpec 变更
- [x] 1.2 明确只支持文本型 PDF，不支持 OCR、Word、Excel、PPT

## 2. 后端实现

- [x] 2.1 引入 Apache PDFBox 依赖
- [x] 2.2 新增文档文本提取组件，支持 Markdown / TXT / PDF
- [x] 2.3 上传配置默认允许 `pdf`
- [x] 2.4 PDF 空文本、加密或解析失败时返回稳定 400
- [x] 2.5 文档 content type 正确标记为 `application/pdf`

## 3. 前端与文档

- [x] 3.1 前端上传入口限制为 `.md,.txt,.pdf`
- [x] 3.2 README 和客户文档同步当前支持边界
- [x] 3.3 演示脚本同步说明文本型 PDF 边界

## 4. 验证

- [x] 4.1 补充 PDF 文本提取单元测试
- [x] 4.2 运行 `./mvnw -q test`
- [x] 4.3 运行 `npm run build`
