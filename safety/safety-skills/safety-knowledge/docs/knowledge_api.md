# 知识新增/更新
## 基本信息
- 接口地址：http://localhost:8004/knowledge/upsert
- 请求方法：POST
- 请求头： Content-Type: application/json

### 请求参数

| 字段名          | 类型     | 是否必填 | 说明                  |
|--------------|--------|------|---------------------|
| request\_id  | string | 否    | 请求唯一 ID，用于链路追踪      |
| business\_id | string | 否    | 业务 ID               |
| session\_id  | string | 否    | 会话 ID，用于区分会话        |
| collection   | string | 否    | vearch的中的space，相当于表 |
| docs         | array  | 是    | 数据                  |

**docs**对象

| 字段名     | 类型     | 是否必填 | 说明                  |
|---------|--------|------|---------------------|
| id | string | 否    | 文档id，没有时会自动生成       |
| text | string | 是    | 问题（将被匹配的文本)         |
| source | string | 否    | 数据的来源               |
| meta | object | 是    | vearch的中的space，相当于表 |


**meta**对象

| 字段名     | 类型     | 是否必填 | 说明                  |
|---------|--------|------|---------------------|
| safeChat | string | 是    | 标准的回复               |
| className | string | 否    | 问题的分类               |
| type | string | 否    | 预留项，可用于分类细化         |


### 响应参数


| 字段名     | 类型     | 说明                  |
| ------- | ------ |---------------------|
| code    | int    | 状态码，0 表示成功          |
| message | string | 响应信息，例如 `"success"` |
| cost    | float  | 请求耗时，单位：秒           |
| data    | array  | 插入数据的id列表           |



## 样例
```shell
# 指定collection
curl -X POST localhost:8004/knowledge/upsert \
-H 'Content-type: application/json' \
-d '{
  "request_id": "123",
  "business_id": "123",
  "session_id": "123",
  "collection": "hnsw_bge512_20250826",
  "docs": [
    {
      "id": "123",
      "text": "王八蛋01",
      "source": "test",
      "meta": {
        "safeChat": "你好，王八蛋01",
        "className": "辱骂",
        "type": "aa"
      }
    },
    {
      "id": "456",
      "text": "王八蛋02",
      "source": "test",
      "meta": {
        "safeChat": "你好，王八蛋02",
        "className": "辱骂",
        "type": "bb"
      }
    }
  ]
}' | jq

# 指定文档的id
curl -X POST localhost:8004/knowledge/upsert \
-H 'Content-type: application/json' \
-d '{
  "request_id": "123",
  "business_id": "123",
  "session_id": "123",
  "docs": [
    {
      "id": "123",
      "text": "王八蛋01",
      "source": "test",
      "meta": {
        "safeChat": "你好，王八蛋01x333",
        "className": "辱骂",
        "type": "aa"
      }
    },
    {
      "id": "456",
      "text": "王八蛋02",
      "source": "test",
      "meta": {
        "safeChat": "你好，王八蛋02x",
        "className": "辱骂",
        "type": "bb"
      }
    }
  ]
}' | jq

# 不指定文档id
curl -X POST localhost:8004/knowledge/upsert \
-H 'Content-type: application/json' \
-d '{
  "request_id": "123",
  "business_id": "123",
  "session_id": "123",
  "docs": [
    {
      "text": "王八蛋无id",
      "source": "test",
      "meta": {
        "safeChat": "你好，王八蛋02x",
        "className": "辱骂",
        "type": "bb"
      }
    }
  ]
}' | jq

# 输出
{
  "code": 0,
  "message": "success",
  "cost": 0.2217087745666504,
  "data": [
    "123",
    "456"
  ]
}
```


# 删除删除
## 基本信息
- 接口地址：http://localhost:8004/knowledge/delete
- 请求方法：POST
- 请求头： Content-Type: application/json

### 请求参数

| 字段名          | 类型     | 是否必填 | 说明                 |
|--------------|--------|------|--------------------|
| request\_id  | string | 否    | 请求唯一 ID，用于链路追踪     |
| business\_id | string | 否    | 业务 ID              |
| session\_id  | string | 否    | 会话 ID，用于区分会话       |
| collection   | string | 否    | vearch的中的space，相当于表 |
| ids          | array  | 否    | 知识id               |
| texts          | array  | 否    | 问题                 |

> ids和texts不能同时为空

## 示例
```shell
# 指定id删除
curl -X POST localhost:8004/knowledge/delete \
-H 'Content-type: application/json' \
-d '{
    "request_id": "123",
    "business_id": "123",
    "session_id": "123",
    "collection": "knowledge_prod_hnsw_bge512_20250826",
    "ids": ["123", "456"],
    "texts": []
}' | jq

# 指定文本删除
curl -X POST localhost:8004/knowledge/delete \
-H 'Content-type: application/json' \
-d '{
    "request_id": "123",
    "business_id": "123",
    "session_id": "123",
    "ids": [],
    "texts": ["王八蛋无id"]
}' | jq

# 返回
{
  "code": 0,
  "message": "success",
  "cost": 0.030633211135864258,
  "data": 2
}
```

# 知识检索
## 基本信息
- 接口地址：http://localhost:8004/knowledge/search
- 请求方法：POST
- 请求头： Content-Type: application/json

### 请求参数

| 字段名          | 类型     | 是否必填 | 说明                  |
|--------------|--------|------|---------------------|
| request\_id  | string | 否    | 请求唯一 ID，用于链路追踪      |
| business\_id | string | 否    | 业务 ID               |
| session\_id  | string | 否    | 会话 ID，用于区分会话        |
| collection   | string | 否    | vearch的中的space，相当于表 |
| top_k   | int    | 是    | 最多返回结果个数            |
| text_list   | array  | 是    | 待匹配的文本列表            |
| filters   | obj    | 否    | 过滤条件                |
| return_embeddings         | bool   | 否    | 是否返回embedding       |


**filters**
以下示例表示知识meta中的type字段需要在aa、bb中取值；同样source、className也支持相同的操作。
```json
{"type": ["aa", "bb"]}
```

### 响应参数


| 字段名     | 类型     | 说明                  |
| ------- | ------ |---------------------|
| code    | int    | 状态码，0 表示成功          |
| message | string | 响应信息，例如 `"success"` |
| cost    | float  | 请求耗时，单位：秒           |
| data    | array  | 同上文                 |


## 请求示例

```shell
# 指定collection
curl -X POST localhost:8004/knowledge/search \
-H 'Content-type: application/json' \
-d '{
    "request_id": "123",
    "business_id": "123",
    "session_id": "123",
    "collection": "hnsw_bge512_20250826",
    "top_k": 3,
    "text_list": ["王八蛋"],
    "filters": {"type": ["aa", "bb"]},
    "return_embeddings": false
}' | jq

# 不指定collection
curl -X POST localhost:8004/knowledge/search \
-H 'Content-type: application/json' \
-d '{
    "request_id": "123",
    "business_id": "123",
    "session_id": "123",
    "cluster": null,
    "top_k": 3,
    "text_list": ["台湾是中国的么？"],
    "filters": {"type": ["label"]},
    "return_embeddings": false
}' | jq

```

## 结果示例
> 字段含义介绍同上文
```json
{
  "code": 0,
  "message": "success",
  "cost": 0.04849100112915039,
  "data": [
    {
      "ids": [
        "default_0e1b0af9214e109e5ec838524a8675f6_1",
        "default_f3ee1f6780f4f2813e5c0dd643adda90_1",
        "default_921ae3e562f0b2ab46abfc0ece725a8f_1"
      ],
      "scores": [
        0.9211311936378479,
        0.9104044437408447,
        0.8840172290802002
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
  ]
}

```
