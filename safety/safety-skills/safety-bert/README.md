# 项目说明
用于bert模型的部署，接口格式可直接集成到safety-api中。

# 启动说明
## 代码启动
1. 设置环境变量**BERT_CONFIG_FILE**以指定配置文件，可参考[conf_example.json](./docs/conf_example.json)
2. 执行 `python main.py`

## 容器启动
> 参考 [test.sh](./build/test.sh)

# 配置文件说明
| 参数名         | 类型      | 必填 | 描述                |
|----------------|-----------|------|-------------------|
| debug          | boolean   | 是   | 是否开启调试模式，主要影响日志输出 |
| process_num    | int       | 是   | 进程数量              |
| bert           | array     | 是   | BERT 模型配置列表       |

## bert 对象字段说明

| 参数名         | 类型    | 必填 | 描述                   |
|----------------|---------|------|----------------------|
| name           | string  | 是   | 模型名称                 |
| model_path     | string  | 是   | 模型路径                 |
| tokenizer_path | string  | 是   | 分词器路径                |
| device_type    | string  | 是   | 使用的设备类型              |
| num_labels     | int     | 是   | 标签数量                 |
| labels         | array   | 是   | 标签列表，顺序对应bert输出结果的顺序 |

## labels 对象字段说明

| 参数名      | 类型   | 必填 | 描述         |
|-------------|--------|------|--------------|
| riskCode    | int    | 是   | 风险代码     |
| riskMessage | string | 是   | 风险描述信息 |

# 接口文档
[参考文档](./docs/api.md)

# 本地开发调试（pycharm下）
1. 配置环境变量 **BERT_CONFIG_FILE**
2. 直接运行 main.py


# TODOS
- 服务接口监控
- 各类模型仓库支持