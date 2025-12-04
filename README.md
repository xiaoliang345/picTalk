# PicTalk

PicTalk 是一个基于 Spring Boot 开发的图片管理系统，支持图片上传、管理、AI 处理等功能。该系统提供了完整的用户认证、权限控制、空间管理和图片处理能力。

## 功能特性

### 核心功能
- 用户注册与登录
- 图片上传（支持本地文件和 URL）
- 图片批量上传
- 图片管理（增删改查）
- 空间管理（创建、编辑、删除个人空间）
- 图片审核机制
- 基于角色的访问控制（RBAC）

### AI 特色功能
- AI 图片处理
- 异步任务处理

### 技术特色
- 多级缓存（Caffeine + Redis）
- 对象存储（腾讯云 COS）
- 分页查询优化
- 异常统一处理
- 全局拦截器

## 技术栈

### 后端技术
- Java 17
- Spring Boot 2.7.6
- MyBatis Plus
- MySQL
- Redis
- Caffeine（本地缓存）
- 腾讯云 COS（对象存储）
- 阿里云 DashScope（AI 服务）
- Hutool（工具库）
- Knife4j（API 文档）
- Lombok

### 架构设计
- MVC 架构模式
- RESTful API 设计
- 统一响应结果封装
- 全局异常处理
- AOP 权限校验

## 项目结构

```
aiPicturesStore/
├── annotation/          注解类
├── aop/                 AOP 切面
├── common/              通用类（响应、异常等）
├── config/              配置类
├── constant/            常量定义
├── controller/          控制器层
├── enums/               枚举类
├── exception/           异常处理
├── manager/             管理器（业务逻辑封装）
├── mapper/              数据访问层
├── model/               数据模型
│   ├── dto/            数据传输对象
│   ├── entity/         实体类
│   └── vo/             视图对象
├── service/            服务层
│   ├── impl/           服务实现
├── utils/              工具类
└── AiPicturesStoreApplication.java  启动类
```

## 主要模块介绍

### 用户模块
- 用户注册、登录、登出
- 用户权限管理（管理员/普通用户）
- 用户信息维护

### 图片模块
- 图片上传（支持文件和 URL）
- 图片批量上传
- 图片编辑（标签、分类等）
- 图片审核机制
- 图片搜索（按颜色等）
- AI 图片处理

### 空间模块
- 个人空间创建与管理
- 空间等级管理
- 空间容量控制

### AI 模块
- AI 图片创建
- AI 图片编辑
- 异步任务处理

## 配置要求

- JDK 17 或以上版本
- MySQL 5.7 或以上版本
- Redis 5.0 或以上版本
- Maven 3.6 或以上版本


## 快速开始

1. **克隆项目**：
   ```bash
   git clone <项目地址>
   ```

2. **创建数据库并导入初始化脚本**  
   创建名为 `picTalk` 的 MySQL 数据库，并执行项目中的 SQL 初始化脚本。

3. **新建环境配置文件**  
   在 `src/main/resources/` 目录下，分别创建以下两个配置文件：

   - **本地开发环境**：`application-local.yml`
   - **生产环境**：`application-prod.yml`

   并在两个文件中填入对应的配置信息（示例如下）：

   ```yaml
   # application-local.yml 或 application-prod.yml
   spring:
     datasource:
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://localhost:3306/picTalk
       username: your_db_username      # 替换为你的数据库用户名
       password: your_db_password      # 替换为你的数据库密码

   cos:
     client:
       host: https://cos.<region>.myqcloud.com  # 替换为实际 COS 访问域名
       secretId: your_secret_id                 # 替换为腾讯云 SecretId
       secretKey: your_secret_key               # 替换为腾讯云 SecretKey
       region: ap-beijing                       # 替换为你的 COS 所在区域，如 ap-shanghai
       bucket: your-bucket-name-1250000000      # 替换为你的存储桶名称（含 APPID）

   # DashScope 配置阿里百炼
   dashscope:
      apiKey: your_dashscope_api_key  # 替换为你的DashScope API Key
      apiUrl: https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation
   ```

   > ⚠️ **重要安全提示**：  
   > - 请勿将真实的敏感信息（数据库密码、API Key、密钥等）提交到版本控制系统
   > - 确保 `application-local.yml` 和 `application-prod.yml` 已添加到 `.gitignore`
   > - 在团队合作中，通过环境变量或密钥管理服务来传递敏感配置信息

   > 💡 提示：  
   > - `application-local.yml` 用于本地开发，可连接本地 MySQL 和测试用 COS；  
   > - `application-prod.yml` 用于生产部署，应填写线上数据库和正式 COS 配置；  
   > - 启动时通过 `--spring.profiles.active=local` 或 `prod` 指定使用哪个配置。

4. **使用 Maven 编译项目**：
   ```bash
   mvn clean install
   ```

5. **启动项目**（以本地环境为例）：
   ```bash
   mvn spring-boot:run --spring.profiles.active=local
   ```

   若部署到生产环境，可打包后运行：
   ```bash
   java -jar target/your-app.jar --spring.profiles.active=prod
   ```
```

或者打包后运行：
```
mvn package
java -jar target/aiPicturesStore-0.0.1-SNAPSHOT.jar
```

## API 文档

项目集成了 Knife4j API 文档，启动项目后访问：
```
http://localhost:8123/doc.html
```

## 部署说明

推荐使用 Docker 部署，或者直接部署 jar 包。

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进项目。