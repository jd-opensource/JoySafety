# safety-admin

## 功能说明

safety-admin 是基于 Spring Boot 的配置与数据管理服务，主要用于大模型安全防护系统的业务、DAG流程、敏感词、知识库等配置管理。支持多业务分组、版本管理、在线/下线切换，具备灵活的原子能力编排和高扩展性。

## 主要亮点

- **多业务分组与版本管理**：支持业务分组隔离、配置多版本升级与回滚。
- **DAG流程编排**：通过 YAML/JSON 灵活定义检测流程，支持原子能力并行/串行组合。
- **原子能力扩展**：内置多种检测能力（关键词、知识库、BERT等），可自定义扩展。
- **高可用配置管理**：支持在线/下线、升级、回滚等操作，保障配置安全与稳定。
- **SSO 单点登录集成**：便于企业级统一认证。
- **丰富 API 文档**：集成 Knife4j，便于在线调试和接口文档查看。
- **多环境日志支持**：基于 Log4j2，支持开发/生产环境灵活配置。

## 使用示例

### 1. 新建业务

```shell
curl -X POST http://localhost:8006/config/defense/manage/business/new \
-H 'Content-type: application/json' \
-d '{
    "name": "test",
    "group": "default",
    "desc": "测试key",
    "type": "toC",
    "secretKey": "4ab8036c-5277-4732-a294-b9575a3bad35",
    "qpsLimit": 20,
    "robotCheckConfObj": {
      "checkNum": 1,
      "unit": "slice_single",
      "timeoutMilliseconds": 5000000
    },
    "userCheckConfObj": {
      "checkNum": 1,
      "unit": "message_single",
      "timeoutMilliseconds": 5000000
    }
  }'
```

### 2. 新建 DAG 流程（YAML）

```shell
curl -X POST http://localhost:8006/config/defense/manage/dag/newDagWithYaml \
-H 'Content-type: text/plain' \
-d '
businessName: test
group: default
desc: test
rootId: start
confArray:
  - nodeId: start
    functionConf:
      type: dummy
      name: dummy
      timeoutMilliseconds: 5
      conf: {}
    routerConf:
      type: groovy
      name: groovy
      conf:
        script: |-
          def from_robot=ctx.curReq[0].fromRobot()
          if (from_robot) {
              return "from_robot_test1"
          } else {
              return "from_user_test1"
          }
    ignoreError: true
'
```

### 3. 业务/流程上线、下线、升级等

- 上线：`/config/defense/manage/business/online` 或 `/config/defense/manage/dag/online`
- 下线：`/config/defense/manage/business/offline` 或 `/config/defense/manage/dag/offline`
- 升级：`/config/defense/manage/business/upgrade` 或 `/config/defense/manage/dag/upgrade`
- 获取生效配置：`/config/defense/manage/business/active`、`/config/defense/manage/dag/active`

详见 `docs/api/` 目录下各接口文档。

## 如何调试

### 本地调试 jar 包

```shell
# 1. debug模式本地启动服务
java -agentlib:jdwp=transport=dt_socket,address=127.0.0.1:9999,suspend=y,server=y \
 -Djdos_group=default -Dspring_profiles_active=pre -jar LlmSecStatisticWeb.jar

# 2. 使用 IDE 开启远程调试，连接 127.0.0.1:9999
```

### 日志配置

- Log4j2 多环境支持，详见 `docs/log4j2-example.xml`。
- 生产环境建议关闭 Console 输出，仅保留文件/远程日志。

### API 文档与在线调试

- 集成 Knife4j，访问 `/doc.html` 查看所有接口及在线调试。

## 目录结构简述

- `src/main/java/com/jd/security/llmsec/contoller/`：各类配置管理接口
- `src/main/java/com/jd/security/llmsec/service/`：业务逻辑实现
- `src/main/java/com/jd/security/llmsec/data/`：数据访问层
- `docs/api/`：接口文档
- `build/`、`Dockerfile`：构建与部署相关

## 依赖环境

- JDK 8+
- MySQL 8+
- Maven 3.8+
- Redis（如需缓存支持）

---

如需更多接口和配置示例，请参考 `docs/api/` 目录下的详细文档。
