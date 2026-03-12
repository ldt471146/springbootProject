# 快速启动和测试指南

## 一、快速启动

### 1. 启动后端服务
```bash
cd D:\aigent\javaweb\SpringBootProject

# 方式1: Maven 启动
mvn clean install
mvn spring-boot:run

# 方式2: IDE 启动（推荐）
# 在 IDE 中打开项目，右键运行 SpringBootProjectApplication.java

# 启动成功标志
# 日志输出: "收到应用启动请求，准备初始化学生实习与实践教学管理系统"
# 访问: http://localhost:8081/doc.html (Swagger 文档)
```

### 2. 启动前端（可选）
```bash
cd src/main/frontend
npm install
npm run dev

# 前端访问: http://localhost:5173
```

---

## 二、运行自动化测试

### 方式1: Python 测试（推荐）
```bash
# 安装依赖
pip install requests

# 运行测试
python test.py

# 输出示例
# ✓ 管理员登录
# ✓ 学生注册
# ✓ 项目创建
# ...
# 测试结果: 通过 14 | 失败 0
```

### 方式2: Bash 测试
```bash
# 需要 curl 和 grep
bash test.sh

# 输出示例
# [TEST] 管理员登录
# [INFO] 管理员登录成功
# ...
```

### 方式3: REST Client 测试（VS Code）
1. 安装 REST Client 扩展
2. 打开 `test-api.http` 文件
3. 逐个点击 "Send Request" 执行

---

## 三、手动测试核心功能

### 1. 用户认证
```bash
# 管理员登录
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"1234567"}'

# 响应示例
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGc...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "role": "ADMIN"
    }
  }
}
```

### 2. 创建实习项目
```bash
curl -X POST http://localhost:8081/api/internship-project \
  -H "Authorization: Bearer YOUR_TEACHER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "暑期实习",
    "company": "字节跳动",
    "maxStudents": 10,
    "startDate": "2026-06-01",
    "endDate": "2026-08-31"
  }'
```

### 3. 查看 API 文档
访问: http://localhost:8081/doc.html

---

## 四、测试场景检查清单

### 认证模块
- [ ] 管理员能成功登录
- [ ] 学生能成功注册和登录
- [ ] 教师能成功注册和登录
- [ ] Token 有效期正确（24小时）
- [ ] 无效 Token 被拒绝

### 项目管理
- [ ] 教师能创建项目
- [ ] 学生能查看开放项目
- [ ] 项目状态流转正确
- [ ] 项目人数限制有效

### 申请审批
- [ ] 学生能提交申请
- [ ] 教师能查看待审批申请
- [ ] 教师能审批通过/拒绝
- [ ] 同一学生不能重复申请

### 日志管理
- [ ] 学生能提交日志
- [ ] 教师能评分日志
- [ ] 日志按周次组织
- [ ] 评分后状态更新

### 报告管理
- [ ] 学生能上传报告
- [ ] 教师能评阅报告
- [ ] 支持多种文件格式
- [ ] 导出数据正确

### 权限控制
- [ ] 学生无法访问管理功能
- [ ] 教师无法修改他人项目
- [ ] API 权限检查有效

---

## 五、常见问题

### Q: 启动时数据库连接失败
**A:** 检查 MySQL 是否运行，修改 `application.yml` 中的数据库配置

### Q: 前端无法加载
**A:** 确保 `npm install` 完成，检查 `npm run build` 是否成功

### Q: Token 过期
**A:** 重新登录获取新 Token，有效期为 24 小时

### Q: 文件上传失败
**A:** 检查 `uploads` 目录是否存在，文件大小是否超过 20MB

### Q: 测试脚本无法连接
**A:** 确保后端服务运行在 8081 端口

---

## 六、性能测试

### 并发测试
```bash
# 使用 Apache Bench
ab -n 1000 -c 100 http://localhost:8081/api/internship-project

# 使用 wrk
wrk -t4 -c100 -d30s http://localhost:8081/api/internship-project
```

### 数据库查询优化
- 检查慢查询日志
- 验证索引使用情况
- 测试大数据量下的性能

---

## 七、测试完成标准

✓ 所有核心功能可用
✓ 无严重 Bug
✓ 权限控制正确
✓ 数据一致性有保障
✓ 性能满足要求
✓ 用户体验良好

---

## 八、文件说明

| 文件 | 说明 |
|------|------|
| `test.py` | Python 自动化测试脚本 |
| `test.sh` | Bash 自动化测试脚本 |
| `test-api.http` | REST Client 测试用例 |
| `TEST_GUIDE.md` | 详细测试指南 |

---

## 九、获取帮助

- **API 文档**: http://localhost:8081/doc.html
- **项目日志**: 查看控制台输出
- **数据库**: 连接 MySQL 查看表结构
