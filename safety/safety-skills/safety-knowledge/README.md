# 项目说明
用于bert模型的部署，接口格式可直接集成到safety-api中。

# 启动说明
## 代码启动
1. 设置环境变量**KG_CONFIG_FILE**以指定配置文件，可参考[config_example.py](./docs/config_example.py)
2. 执行 `python main.py`

## 容器启动
参考 [test.sh](./build/test.sh)

# 配置文件说明
参考 [config_example.py](./docs/config_example.py)中注释

## vearch中建表
参考 [vearch_manage.py](./docs/vearch_manage.py)

# 接口文档
- [知识库相关操作](./docs/knowledge_api.md)
- [基于知识库的RAG](./docs/answer_api.md)
- [多轮对话检测](./docs/dialog_api.md)

# 本地开发调试（pycharm下）
1. 配置环境变量 **KG_CONFIG_FILE**
2. 直接运行 `main.py`


# TODOS
- 支持其它embedding模型
- 支持其它向量搜索引擎