# MD-Agent

MD-Agent 是一个高度可配置的企业级数据集成引擎，支持 ETL（Extract-Transform-Load）操作，可连接多种异构数据源进行数据交换和同步。

## 功能特性

- **多数据源支持** - SQL Server、Oracle、PostgreSQL、MySQL、GaussDB、SQLite、MS Access
- **消息队列集成** - Kafka、RabbitMQ、Apache Pulsar、MQTT
- **企业系统对接** - SAP RFC 远程函数调用
- **物联网支持** - Huawei IoT SDK 集成
- **JavaScript 脚本转换** - 使用 JS 脚本进行灵活的数据转换
- **定时任务** - 支持 Cron 表达式配置定时执行
- **实时 API 代理** - 动态路由和接口鉴权
- **告警通知** - 邮件、飞书机器人等多种告警方式

## 技术栈

- Java 8
- Spring Boot 2.3.7
- Spring Data JPA
- Druid 连接池
- Apache Kafka 3.6.1
- Apache Pulsar 2.11.4
- MongoDB（可选）
- Nashorn JavaScript Engine

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.x

### 构建项目

```bash
mvn clean package -DskipTests
```

### 运行

```bash
java -jar target/md-agent-1.0.0-SNAPSHOT.jar
```

服务默认启动在 `http://localhost:8080`

### 配置

主要配置文件位于 `src/main/resources/application.yml`：

```yaml
# 数据库配置（默认使用 SQLite）
spring:
  datasource:
    url: jdbc:sqlite:/db/bcp.db

# MongoDB 配置（可选）
ag:
  mongodb:
    enabled: false
```

## 核心模块

| 模块 | 说明 |
|------|------|
| `engine` | 集成引擎核心，包含输入、转换、输出节点 |
| `datasource` | 数据源管理，支持多种数据库和消息队列 |
| `task` | 定时任务调度 |
| `proxy` | API 代理和路由 |
| `email` | 邮件告警服务 |
| `sap` | SAP RFC 连接管理 |

## 工作流程

```
┌─────────┐    ┌───────────┐    ┌──────────┐
│  Input  │───▶│ Transform │───▶│  Output  │
└─────────┘    └───────────┘    └──────────┘
     │              │                 │
     ▼              ▼                 ▼
  数据源        JS 脚本          目标系统
```

1. **输入节点** - 从数据库、API、消息队列等读取数据
2. **转换节点** - 使用 JavaScript 脚本进行数据转换和处理
3. **输出节点** - 将处理后的数据写入目标系统

## 应用场景

- 多系统间的数据交换和同步
- ETL 数据抽取、转换、加载
- IoT 设备数据采集和处理
- API 网关和接口代理
- 消息队列的生产消费

## 贡献

欢迎贡献代码！请阅读 [贡献指南](CONTRIBUTING.md) 了解如何参与项目开发。

## 许可证

本项目采用 [MIT License](LICENSE) 开源协议。
