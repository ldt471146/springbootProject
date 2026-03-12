## 背景

现有日志模块仅支持文本正文提交与教师评阅，不支持学生补充上传 PDF 或 DOCX 材料。报告模块已经具备附件上传、受保护预览与 DOCX 在线渲染能力，本次扩展优先复用已有能力，避免引入第二套文件模型。

## 目标

- 学生提交日志时可为日志绑定多个 PDF/DOCX 附件
- 教师评阅日志时可查看正文和附件，并支持在线预览
- 日志详情、列表、时间线统一返回附件列表
- 旧数据库可直接兼容，不要求手工改表

## 方案

### 数据模型

- 不新增日志附件表，不修改 `intern_journal`
- 继续复用 `sys_attachment`
- 日志附件使用：
  - `ref_type = JOURNAL_ATTACHMENT`
  - `ref_id = journal.id`

### 后端接口

- 在日志模块补充附件接口：
  - `POST /api/journal/{id}/attachments`
  - `DELETE /api/journal/attachments/{attachmentId}`
  - `GET /api/journal/attachments/{attachmentId}/file`
  - `GET /api/journal/attachments/{attachmentId}/preview`
- `JournalVO` 增加 `attachments`
- 权限规则：
  - 学生只能上传和删除自己的日志附件
  - 教师只能查看自己指导学生的日志附件

### 前端交互

- 学生端日志编辑页增加多附件上传区、附件列表、预览与删除入口
- 教师端日志评阅弹窗增加附件区，并支持在线预览
- 预览能力复用现有 `FilePreview` 与受保护 blob 打开链路

### 数据库兼容

- `schema.sql` 继续保留 `sys_attachment(ref_type, ref_id)` 索引
- 启动自检补充附件索引校验，旧库缺少索引时自动补齐

## 验证

- 后端编译通过
- 前端构建通过
- 手工验证学生上传、编辑追加、教师预览、学生删除四条链路
