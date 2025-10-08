# safety-admin

## 功能说明

safety-admin 是基于 Spring Boot 的配置与数据管理服务，主要用于大模型安全防护系统的业务、DAG流程、敏感词、知识库等配置管理。支持多业务分组、版本管理、在线/下线切换，具备灵活的原子能力编排和高扩展性。

## 主要亮点

- **多业务分组与版本管理**：支持业务分组隔离、配置多版本升级与回滚。
- **DAG流程编排**：通过 YAML/JSON 灵活定义检测流程，支持原子能力并行/串行组合。
- **原子能力扩展**：内置多种检测能力（关键词、知识库、BERT等），可自定义扩展。
- **高可用配置管理**：支持在线/下线、升级、回滚等操作，保障配置安全与稳定。
- **丰富 API 文档**：集成 Knife4j，便于在线调试和接口文档查看。
- **多环境日志支持**：基于 Log4j2，支持开发/生产环境灵活配置。

## 使用示例
接口调用具体见[接口文档](docs/api/README.md)

## 启动方式

```shell
# idea中启动，配置以下启动参数，样例参考docs中相应文件
--spring.config.location=$ADMIN_APP_CONF_FILE --logging.config=file:$ADMIN_LOG_CONF_FILE

# 命令行启动
java $JAVA_OPTS -jar {jar路径} \
                --spring.config.location=$ADMIN_APP_CONF_FILE \
                --logging.config=file:$ADMIN_LOG_CONF_FILE

```

### API 文档与在线调试

- 集成 Knife4j，访问 `/doc.html` 查看所有接口及在线调试。

## 依赖环境

- JDK 8
- MySQL 8+
- Maven 3.8+
