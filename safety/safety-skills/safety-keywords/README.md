# 项目说明
关键词服务的，提供标准接口，可直接集成到safety-api中。

# 敏感词数据准备
- 敏感词数据的管理统一在**safety-admin**

# 启动说明
## 代码启动
1. 设置环境变量**KEYWORD_APP_CONF_FILE**以指定springboot配置文件，可参考[application_example.properties](./docs/application_example.properties)
2. 设置环境变量**KEYWORD_LOG_CONF_FILE**以指定log4j2日志配置文件，可参考[log4j2-example.xml](./docs/log4j2-example.xml)
3. 执行 
```shell
java $JAVA_OPTS -jar /work/LlmSecSensitiveWeb.jar \
--spring.config.location=$KEYWORD_APP_CONF_FILE \
--logging.config=file:$KEYWORD_LOG_CONF_FILE
```

## 容器启动
参考 [test.sh](./build/test.sh)

# 接口文档
- [api](./docs/api.md)

# 本地开发调试(idea下)
> 不赘述，同普通的springboot项目

# TODOS
- 支持rpc长连接



