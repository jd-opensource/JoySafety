# 更新/新增红线知识（upsert）

## 请求方法
- 请求地址：/data/redline_knowledge/upsert
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段 | 类型     | 必填 | 说明                     |
| ---- |--------|----|------------------------|
| businessScene | string | 是  | 业务场景（对应业务名称）           |
| group | string | 是  | 业务所属分组                 |
| question | string | 是  | 问题                     |
| answer | string | 是  | 参考答案                   |
| metadata | number | 否  | 附加信息                   |
| className | string | 否  | 类别                     |
| classNo | string | 是  | 类别编码                   |
| source | string | 是  | 数据来源                   |
| status | string | 是  | online(有效)，offline(无效) |
| desc  | string | 是  | 描述                     |

## 请求示例
```shell
curl -X POST http://localhost:8006/data/redline_knowledge/upsert \
-H 'Content-type: application/json' \
-d '{
  "businessScene": "default",
  "question": "你好1",
  "answer": "你好1",
  "metadata": "{\"a\":1}",
  "className": "test",
  "classNo": 0,
  "source": "test",
  "status": "online",
  "desc": "测试一下"
}'
```

## 响应参数

**Body(JSON)**

| 字段名     | 类型     | 说明                                   |
| ------- |--------|--------------------------------------|
| code    | int    | 状态码，0 表示成功                           |
| message | string | 响应信息，例如 `"success"`                  |
| cost    | float  | 请求耗时，单位：秒                            |
| data    | object | 相同字段含义同输入，id字段为自动生成的唯一id，其它字段为预留，可忽略 |


## 结果示例
```json
{
  "code": 0,
  "message": "success",
  "cost": 0,
  "data": {
    "id": 1,
    "uniqId": "43facd6895994ee3b6c59def25544195",
    "businessScene": "default",
    "className": "test",
    "classNo": 0,
    "source": "test",
    "version": 1,
    "status": "online",
    "createTime": 1722227507000,
    "updateTime": 1722227507000,
    "question": "你好1",
    "answer": "你好1",
    "metadata": "{\"a\":1}",
    "desc": "测试一下"
  }
}
```

# 根据id获取红线知识（getById）

## 请求方法
- 请求地址：/data/redline_knowledge/getById
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段             | 类型     | 必填 | 说明                                                                                                                                                                                                                                                                                                                   |
|----------------|--------|----|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| id             | number | 是  | 红线知识id                                                                                                                                                                                                                                                                                                                | |

## 请求示例
```shell
curl -X POST http://localhost:8006/data/redline_knowledge/getById \
-H 'Content-type: application/json' \
-d '{
   "id": 1
}'
```

## 响应参数
同 `upset接口`

# 根据词模糊查询红线知识库（queryByWord）

## 请求方法
- 请求地址：/data/redline_knowledge/queryByWord
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段             | 类型            | 必填 | 说明               |
|----------------|---------------|----|------------------|
| questions             | array[string] | 否  | 问题               | |
| uniqIds             | array[string] | 否  | 问题               | |
| businessScene             | string        | 否  | 业务场景             | |

## 请求示例1
```shell
curl -X POST http://localhost:8006/data/redline_knowledge/getByWord \
-H 'Content-type: application/json' \
-d '{
   "uniqIds": ["fda17c3dba112b56698e0bd590973818"],
   "businessScene": "default"
}'
```

## 返回示例1
```json
{
  "code": 0,
  "message": "success",
  "cost": 0,
  "data": {
    "fda17c3dba112b56698e0bd590973818": {
      "id": 1,
      "uniqId": "fda17c3dba112b56698e0bd590973818",
      "businessScene": "default",
      "className": "test2",
      "classNo": 0,
      "source": "test2",
      "version": 1,
      "status": "online",
      "createTime": 1722198707000,
      "updateTime": 1722198811000,
      "question": "你好2",
      "answer": "你好2",
      "metadata": "{\"a\":2}",
      "desc": "测试一下2"
    }
  }
}
```

## 请求示例2
```shell
curl -X POST http://localhost:8006/data/redline_knowledge/getByWord \
-H 'Content-type: application/json' \
-d '{
   "questions": ["你好2"]
}'
```

## 返回示例2
```json
{
  "code": 0,
  "message": "success",
  "cost": 0,
  "data": {
    "你好2": {
      "id": 1,
      "uniqId": "fda17c3dba112b56698e0bd590973818",
      "businessScene": "default",
      "className": "test2",
      "classNo": 0,
      "source": "test2",
      "version": 1,
      "status": "online",
      "createTime": 1722198707000,
      "updateTime": 1722198811000,
      "question": "你好2",
      "answer": "你好2",
      "metadata": "{\"a\":2}",
      "desc": "测试一下2"
    }
  }
}
```

## 响应参数
返回为list，字段含义参考 `upset接口`

# 红线知识上线（online）

## 请求方法
- 请求地址：/data/redline_knowledge/online
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段             | 类型     | 必填 | 说明                                                                                                                                                                                                                                                                                                                   |
|----------------|--------|----|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| id             | number | 是  | 红线知识id                                                                                                                                                                                                                                                                                                                | |

## 请求示例
```shell
curl -X POST http://localhost:8006/data/redline_knowledge/online \
-H 'Content-type: application/json' \
-d '{
   "id": 1
}'
```

## 响应参数
同 `upset接口`

# 红线知识上线（offline）

## 请求方法
- 请求地址：/data/redline_knowledge/offline
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段             | 类型     | 必填 | 说明                                                                                                                                                                                                                                                                                                                   |
|----------------|--------|----|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| id             | number | 是  | 红线知识id                                                                                                                                                                                                                                                                                                                | |

## 请求示例
```shell
curl -X POST http://localhost:8006/data/redline_knowledge/offline \
-H 'Content-type: application/json' \
-d '{
   "id": 1
}'
```

## 响应参数
同 `upset接口`