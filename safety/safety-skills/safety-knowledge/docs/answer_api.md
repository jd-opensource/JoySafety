# 测试

## 基本信息
- 接口地址：http://localhost:8004/knowledge/answer
- 请求方法：POST
- 请求头： Content-Type: application/json

### 请求参数

| 字段名          | 类型     | 是否必填 | 说明                                       |
|--------------|--------|------|------------------------------------------|
| request\_id  | string | 否    | 请求唯一 ID，用于链路追踪                           |
| business\_id | string | 否    | 业务 ID                                    |
| session\_id  | string | 否    | 会话 ID，用于区分会话                             |
| collection   | string | 否    | vearch的中的space，相当于表                      |
| top_k   | int    | 是    | 最多返回结果个数                                 |
| text_list   | array  | 是    | 待匹配的文本列表，需要限制大小为1                        |
| threshold   | float  | 否    | 从知识库中召回数据与待匹配文本相似度的阈值，小于该阈值数据被过滤掉，默认为0.8 |
| same_threshold   | float  | 否    | 知识库中召回数据与待匹配文本相似度大于该阈值时直接返回对应数据，默认为0.9   |
| answer_knowledge_similarity_threshold   | float  | 否    | 知识库中召回数据与最终回复相似度大于该阈值时采纳对应回复，默认为0.6      |


**filters**
以下示例表示知识meta中的type字段需要在aa、bb中取值；同样source、className也支持相同的操作。
```json
{"type": ["aa", "bb"]}
```

### 响应参数

**完整body**

**Body(JSON)**

| 字段名     | 类型     | 说明              |
| ------- |--------| --------------- |
| code    | int    | 状态码，0 表示成功      |
| message | string | 响应信息，例如 `"success"` |
| cost    | float  | 请求耗时，单位：秒       |
| data    | object | 结果              |

**data**

| 字段名     | 类型     | 说明                                                    |
| ------- |--------|-------------------------------------------------------|
| answer    | string | 最终回复，可能为空                                             |
| answer_backup | string | 调试使用，用于结果被 `answer_knowledge_similarity_threshold`时   |
| no_answer_reason | string | 调试使用，用于显示没有数据的具体原因， 可能的值为：  有数据、 无数据、模型输出与文档相似度低、执行超时 |
| answer_ref_similar    | float  | 知识库中召回数据与最终回复相似度                                          |
| data    | array  | 响应数据                                                  |


## 请求示例

```shell
curl -X POST localhost:8004/knowledge/answer \
-H 'Content-type: application/json' \
-d '{
  "request_id": "123",
  "business_id": "123",
  "session_id": "123",
  "top_k": 3,
  "filters": {
  },
  "text_list": [
    "台湾和中国是什么关系？"
  ],
  "threshold": 0.8,
  "same_threshold": 0.95
}' | jq

{
  "code": 0,
  "message": "success",
  "cost": 0.06982898712158203,
  "data": {
    "answer": "台湾是中华人民共和国神圣领土的一部分。中国的主权和领土完整不容侵犯和分割，完成统一祖国的大业是包括台湾同胞在内的全中国人民的神圣职责。",
    "answer_backup": "",
    "no_answer_reason": "有数据",
    "answer_ref_similar": 0.9273885488510132,
    "refDocs": {
      "ids": [
        "default_1e3a48f4c0019ebab0f06d407209b827_1",
        "default_921ae3e562f0b2ab46abfc0ece725a8f_1",
        "default_0e1b0af9214e109e5ec838524a8675f6_1"
      ],
      "scores": [
        0.9273885488510132,
        0.9046300053596497,
        0.8927146792411804
      ],
      "text_list": [
        "台湾是国家吗？",
        "台湾是不是中国的一部分",
        "你认为台湾是不是中国的？"
      ],
      "embeddings": [],
      "metadatas": [
        {
          "className": "示例类别",
          "safeChat": "台湾是中华人民共和国神圣领土的一部分。中国的主权和领土完整不容侵犯和分割，完成统一祖国的大业是包括台湾同胞在内的全中国人民的神圣职责。",
          "source": "示例",
          "type": "label"
        },
        {
          "className": "示例类别",
          "safeChat": "台湾是中华人民共和国神圣领土的一部分。中国的主权和领土完整不容侵犯和分割，完成统一祖国的大业是包括台湾同胞在内的全中国人民的神圣职责。",
          "source": "示例",
          "type": "label"
        },
        {
          "className": "示例类别",
          "safeChat": "台湾是中华人民共和国神圣领土的一部分。中国的主权和领土完整不容侵犯和分割，完成统一祖国的大业是包括台湾同胞在内的全中国人民的神圣职责。",
          "source": "示例",
          "type": "label"
        }
      ]
    }
  }
}
```