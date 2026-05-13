# 教务管理系统

## 项目简介

本项目使用 RuoYi-Vue 作为基础框架，在其权限认证、用户角色、动态菜单等基础能力上，扩展了教务业务模块。系统前端采用 Vue 2 + Element UI，后端采用 Spring Boot + Spring Security + MyBatis，数据库使用 MySQL，登录状态使用 JWT，缓存依赖 Redis。

## 功能简介

### 管理端

- 管理首页：查看教务数据概览和系统运行入口。
- 基础数据管理：维护院系、学生、教师、课程等基础信息。
- 教学安排管理：维护开课信息，管理课程容量、授课教师、上课时间和地点。
- 选课与成绩管理：查看和维护学生选课记录，支持成绩相关数据管理。
- 开课申请审核：查看教师提交的开课申请，支持审核通过、审核拒绝、填写拒绝原因；审核通过后生成课程与开课安排。
- 学期与全局控制：维护学期数据，设置当前学期，控制学生选课开关和教师登分开关。
- 通知公告发布，日志管理

### 学生端

- 学生首页：展示学生端常用入口和个人教务信息。
- 在线选课：按课程名称、课号、星期、时间段、容量状态筛选当前学期课程，并完成选课。
- 我的课程：查看已选课程、授课教师、上课时间、地点、学分和选课状态，支持退课。
- 成绩查询：按学年和学期查询课程成绩，展示平时成绩、考试成绩、总评成绩和绩点。
- 课表查询：查看个人课程安排，辅助确认上课时间和地点。

### 教师端

- 教师首页：展示教师端常用入口，包括当前授课、历史授课和开课申请。
- 当前学期授课：查看当前学期课程、上课时间、选课人数，支持查看学生名单。
- 历史学期授课：查询历史授课记录和课程学生名单。
- 成绩登分：对授课课程的学生录入平时成绩、考试成绩和总评成绩，支持成绩权重设置、单条保存和批量保存。
- 开课申请：教师可选择已有课程或新建课程提交开课申请，填写学期、学分、学时、院系、期望时间、容量和申请理由，并查看审核状态。

## 技术栈

**后端**

- Java 17
- Spring Boot 4.0.3
- Spring Security
- MyBatis
- Druid
- MySQL
- Redis
- JWT
- Quartz

**前端**

- Vue 2.6
- Vue Router
- Vuex
- Element UI
- Axios
- ECharts

## 项目结构

```text
.
├── ruoyi-admin       # 后端启动模块
├── ruoyi-common      # 通用工具模块
├── ruoyi-framework   # 框架核心模块
├── ruoyi-generator   # 代码生成模块
├── ruoyi-quartz      # 定时任务模块
├── ruoyi-system      # 系统与教务业务模块
├── ruoyi-ui          # Vue 前端项目
└── sql
    ├── schema.sql    # 创建数据库和表结构
    └── seed.sql      # 插入当前测试数据
```

## 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 14+ 推荐
- MySQL 5.7+ / 8.0+
- Redis 5+

## 数据库初始化

项目只保留两个 SQL 文件：

- `sql/schema.sql`：创建数据库和表结构
- `sql/seed.sql`：插入测试数据

按顺序执行：

```bash
mysql -uroot -p < sql/schema.sql
mysql -uroot -p < sql/seed.sql
```

默认数据库名为 `ry-vue`。如果需要修改数据库名，请同步修改：

- `sql/schema.sql`
- `sql/seed.sql`
- `ruoyi-admin/src/main/resources/application-druid.yml`

## 触发器与存储过程说明

为满足课程设计中“至少一个触发器和一个存储过程在系统中使用和调用”的要求，`sql/schema.sql` 已内置以下数据库对象：

1. 触发器（`edu_enrollment` 选课表）
   - `trg_enrollment_after_insert`
   - `trg_enrollment_after_delete`
   - `trg_enrollment_after_update`
   - 作用：当选课记录新增/删除/状态变更时，自动维护 `edu_course_offering.selected_count`（开课已选人数），避免前后端重复计算导致数据不一致。

2. 存储过程
   - `sp_apply_commit`
   - 作用：用于开课申请审核通过时的一体化落库逻辑（课程匹配/创建、开课信息写入、申请状态更新等），保证流程原子性，减少跨多条 SQL 的中间状态问题。

说明：只要按 README 的初始化顺序执行 `schema.sql` 和 `seed.sql`，上述触发器和存储过程会自动创建，无需手动再建。

## 后端启动

1. 修改数据库连接配置：

```yaml
# ruoyi-admin/src/main/resources/application-druid.yml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
        username: root
        password: your_password
```

2. 确认 Redis 已启动，默认连接地址为 `localhost:6379`。

3. 编译并启动后端：

```bash
mvn -pl ruoyi-admin -am clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

也可以在 IDE 中直接运行：

```text
ruoyi-admin/src/main/java/com/ruoyi/RuoYiApplication.java
```

后端默认地址：

```text
http://localhost:8080
```

接口文档地址：

```text
http://localhost:8080/swagger-ui.html
```

## 前端启动

进入前端目录：

```bash
cd ruoyi-ui
npm install
npm run dev
```

前端开发服务默认端口为 `80`，接口代理配置在：

```text
ruoyi-ui/vue.config.js
```

默认代理到：

```text
http://localhost:8080
```

## 默认账号

测试数据中包含管理员、教师、学生账号。常用登录账号：

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin123` |
| 教师 | `T001` | `admin123` |
| 学生 | `S002` | `admin123` |

部分测试账号可能已经在调试过程中修改过密码，若无法登录，可使用管理员账号在用户管理中重置密码。

## 常用命令

后端编译：

```bash
mvn -pl ruoyi-admin -am -DskipTests compile
```

后端打包：

```bash
mvn -pl ruoyi-admin -am clean package -DskipTests
```

前端开发：

```bash
cd ruoyi-ui
npm run dev
```

前端生产打包：

```bash
cd ruoyi-ui
npm run build:prod
```

## 常见问题

1. 登录验证码或缓存异常：确认 Redis 已启动，并检查 `ruoyi-admin/src/main/resources/application.yml` 中 Redis 地址和端口。
2. 后端启动失败提示数据库连接错误：确认 MySQL 已启动，数据库账号密码和 `application-druid.yml` 一致。
3. 前端请求 404 或无法登录：确认后端运行在 `8080` 端口，并检查 `ruoyi-ui/vue.config.js` 代理配置。
4. 菜单不显示：确认已经导入 `sql/seed.sql`，其中包含系统菜单和角色菜单权限数据。

## 说明

本项目基于开源项目 RuoYi-Vue 进行课程设计/教务场景改造，仅用于学习与教学管理系统实践。
