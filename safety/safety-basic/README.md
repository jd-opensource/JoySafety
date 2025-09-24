# safety-basic

## 功能说明

safety-basic 是大模型安全防护系统的核心能力库，聚焦于安全检测、原子能力编排、DAG流程引擎、会话与鉴权等基础能力。为上层配置管理、API服务等模块提供高性能、可扩展的安全检测与流程编排能力。

## 主要亮点

- **原子能力丰富**：内置关键词、知识库、BERT、RAG、标签预测、多轮检测等多种安全检测能力。
- **DAG流程引擎**：支持节点式原子能力编排，灵活路由（Groovy脚本、关键词、用户/机器人终止等）。
- **高扩展性**：抽象 Function/Router/CheckResult 等核心接口，便于自定义扩展能力。
- **会话与鉴权支持**：内置会话上下文、业务类型、鉴权配置等，适配多场景安全需求。
- **高性能设计**：异步检测、并行能力、风险聚合等机制，提升检测效率。
- **丰富工具类**：MD5、签名、标签处理、Groovy 脚本等工具，便于集成和二次开发。

## 目录结构简述

- `core/check/`：各类安全检测能力实现
- `core/conf/`：配置模型与原子能力参数
- `core/engine/`：DAG流程编排与执行引擎
- `core/session/`：会话、消息、业务类型等上下文模型
- `core/auth/`：鉴权相关能力
- `core/util/`：工具类（加密、标签、Groovy等）
- `core/openai/`：OpenAI 相关模型与接口
- `core/rpc/`：API 服务接口定义

## 使用示例

### 1. 编译与安装

```shell
mvn clean package install
```

### 2. 典型能力调用（伪代码）

```java
// 构建检测请求
RequestBase req = new RequestBase(...);
// 构建 DAG 流程
FunctionChainBuilder builder = new FunctionChainBuilder(...);
Node root = builder.build(...);
// 执行检测
FunctionExecutor executor = new FunctionExecutor();
RiskCheckResult result = executor.execute(root, req);
```

### 3. 扩展自定义能力

继承 AbstractFunction 或 AbstractRouter，实现自定义检测/路由逻辑，注册到流程编排即可。

## 如何调试

- 单元测试：推荐使用 JUnit 5，参考 `core/util/SignUtilTest.java`。
- 日志：集成 SLF4J，便于与主工程统一日志输出。
- 断点调试：可在 IDE 中直接调试核心流程与自定义能力。

## 依赖环境

- JDK 8+
- Maven 3.8+
- Lombok（开发环境需安装插件）
- SLF4J、Guava、Fastjson2、Apache Commons、POI 等


## 如何发布到中央仓库
参考：https://central.sonatype.org/register/central-portal/
```shell
mvn clean package -P release install deploy -Pgpg -Dgpg.passphrase={你的gpg密码}
```


---

如需更多扩展与集成示例，请参考源码及各核心接口文档。
