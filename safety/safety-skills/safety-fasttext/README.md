# 项目说明
用于fasttext模型的部署，接口格式可直接集成到safety-api中。

# 启动说明
## 代码启动
1. 设置环境变量**FASTTEXT_GUNICORN_CONF**以指定gunicorn配置文件，如果没有配置，默认使用[gunicorn.conf.py](gunicorn.conf.py)
2. 设置环境变量**FASTTEXT_CONFIG**以修改模型配置，格式参考：[conf_example.json](./docs/conf_example.json)
3. 执行 `gunicorn -c "${FASTTEXT_GUNICORN_CONF:-./gunicorn.conf.py}"  main:app`

## 容器启动
> 参考 [test.sh](./build/test.sh)

# 接口文档
[参考文档](./docs/api.md)

# 本地开发调试（pycharm下）
1. 设置环境变量**FASTTEXT_CONFIG**以修改模型配置，格式参考：[conf_example.json](./docs/conf_example.json)
2. 配置环境变量**PYCHARM_DEBUG=1**
3. 直接运行 main.py



# TODOS
- 服务接口监控
- 各类模型仓库支持