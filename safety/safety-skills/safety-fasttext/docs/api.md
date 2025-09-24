# 接口文档
## 基本信息
- 接口地址：http://localhost:8002/fasttext/{model_name}
- 请求方法：POST
- 请求头： Content-Type: application/json

请求参数

| 字段名          | 类型     | 是否必填 | 说明             |
| ------------ | ------ |------| -------------- |
| model\_name  | string | 否    | 对应配置文件中配置的模型名  |
| request\_id  | string | 否    | 请求唯一 ID，用于链路追踪 |
| business\_id | string | 否    | 业务 ID          |
| session\_id  | string | 否    | 会话 ID，用于区分会话   |
| text\_list   | array  | 是    | 待处理文本列表        |

示例
```json
{
  "request_id": "123",
  "business_id": "123",
  "session_id": "123",
  "text_list": ["你好"]
}
```


## 响应参数

**Body(JSON)**

| 字段名     | 类型     | 说明                  |
| ------- | ------ | ------------------- |
| code    | int    | 状态码，0 表示成功          |
| message | string | 响应信息，例如 `"success"` |
| cost    | float  | 请求耗时，单位：秒           |
| data    | array  | 检测结果列表              |


**data** 数组元素

| 字段名         | 类型     | 说明            |
| ----------- | ------ | ------------- |
| riskCode    | int    | 风险代码，0 表示正常   |
| riskMessage | string | 风险说明          |
| probability | float  | 预测概率          |
| detail      | array  | 详细检测结果（结构同本级） |
| text        | string | 对应输入的文本       |



# 完整示例
## 输入
```shell
curl http://localhost:8002/fasttext/fast20250710 \
-H 'Content-type: application/json' \
-d '{
    "request_id": "123",
    "business_id": "123",
    "session_id": "123",
    "text_list": ["你好"]
}' | jq
```

## 输出
```json
{
  "code": 0,
  "message": "ok",
  "cost": 0.687713623046875,
  "data": [
    {
      "riskCode": 0,
      "riskMessage": "正常文本",
      "probability": 1.0000100135803223,
      "detail": [
        {
          "riskCode": 0,
          "riskMessage": "正常文本",
          "probability": 1.0000100135803223
        },
        {
          "riskCode": 2,
          "riskMessage": "业务场景",
          "probability": 1.0000003385357559e-05
        },
        {
          "riskCode": 1,
          "riskMessage": "恶意",
          "probability": 1.0000003385357559e-05
        }
      ],
      "text": "你好"
    }
  ]
}
```