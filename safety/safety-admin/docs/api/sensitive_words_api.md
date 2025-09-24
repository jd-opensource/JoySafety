# 更新/新增敏感词（upsert）

## 请求方法
- 请求地址：/data/sensitive_words/upsert
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段 | 类型     | 必填 | 说明                                                                                                                                                                                                                                                                                                                   |
| ---- |--------|----|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| businessScene | string | 是  | 业务场景（对应业务名称）                                                                                                                                                                                                                                                                                                         |
| group | string | 是  | 业务所属分组                                                                                                                                                                                                                                                                                                               |
| word | string | 是  | 敏感词                                                                                                                                                                                                                                                                                                                  |
| className | string | 是  | 类别名称（可自己定义）                                                                                                                                                                                                                                                                                                          |
| classNo | number | 是  | 类别编码（可自己定义）                                                                                                                                                                                                                                                                                                          |
| tags | string | 否  | 预留，用于类别细分                                                                                                                                                                                                                                                                                                            |
| matchType | string | 是  | 匹配类别，枚举值：<pre><code>     <br>     /\*\*<br>     \* 包含指定词<br>     \*/<br>    contain,<br><br>    /\*\*<br>     \* 完全相等<br>     \*/<br>    equal,<br><br>    /\*\*<br>     \* 包含正则指定的内容<br>     \*/<br>    regex,<br><br>    /\*\*<br>     \* 排除（用于在特定业务下排除通用的词）<br>     \*/<br>    exclude,<br>    ;<br></pre></code> |
| handleStrategy | string | 是  | 处置策略，枚举值为：black(黑名单)、white(白名单)、gray(灰名单)                                                                                                                                                                                                                                                                            |
| source | string | 是  | 数据来源                                                                                                                                                                                                                                                                                                                 |
| status | string | 是  | online(有效)，offline(无效)                                                                                                                                                                                                                                                                                               |
| desc  | string | 是  | 描述                                                                                                                                                                                                                                                                                                                   |

## 请求示例
```shell
curl -X POST http://localhost:8006/data/sensitive_words/upsert \
-H 'Content-type: application/json' \
-d '{
  "businessScene": "default",
  "word": "你好1",
  "className": "test",
  "classNo": 0,
  "tags": "test",
  "matchType": "equal",
  "handleStrategy": "block",
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
    "id": 2,
    "uniqId": "43facd6895994ee3b6c59def25544195",
    "businessScene": "default",
    "word": "你好1",
    "className": "test",
    "classNo": 0,
    "tags": "test",
    "matchType": "equal",
    "handleStrategy": "block",
    "source": "test",
    "version": 1,
    "status": "online",
    "createTime": 1722220096000,
    "updateTime": 1722220096000,
    "desc": "测试一下"
  }
}
```

# 根据id获取敏感词（getById）

## 请求方法
- 请求地址：/data/sensitive_words/getById
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段             | 类型     | 必填 | 说明                                                                                                                                                                                                                                                                                                                   |
|----------------|--------|----|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| id             | number | 是  | 敏感词id                                                                                                                                                                                                                                                                                                                | |

## 请求示例
```shell
curl -X POST http://localhost:8006/data/sensitive_words/getById \
-H 'Content-type: application/json' \
-d '{
   "id": 1
}'
```

## 响应参数
同 `upset接口`

# 根据词模糊查询敏感词库（queryByWord）

## 请求方法
- 请求地址：/data/sensitive_words/queryByWord
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段             | 类型     | 必填 | 说明               |
|----------------|--------|----|------------------|
| word             | string | 是  | 搜索词              | |
| status             | string | 否  | online / offline | |
| businessScene             | string | 否  | 业务场景             | |

## 请求示例
```shell
curl -X POST http://localhost:8006/data/sensitive_words/queryByWord \
-H 'Content-type: application/json' \
-d '{
    "word": "杀",
    "status": "online",
    "businessScene": "xxx"
}'
```

## 响应参数
返回为list，字段含义参考 `upset接口`

# 敏感词上线（online）

## 请求方法
- 请求地址：/data/sensitive_words/online
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段             | 类型     | 必填 | 说明                                                                                                                                                                                                                                                                                                                   |
|----------------|--------|----|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| id             | number | 是  | 敏感词id                                                                                                                                                                                                                                                                                                                | |

## 请求示例
```shell
curl -X POST http://localhost:8006/data/sensitive_words/online \
-H 'Content-type: application/json' \
-d '{
   "id": 1
}'
```

## 响应参数
同 `upset接口`

# 敏感词上线（offline）

## 请求方法
- 请求地址：/data/sensitive_words/offline
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段             | 类型     | 必填 | 说明                                                                                                                                                                                                                                                                                                                   |
|----------------|--------|----|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| id             | number | 是  | 敏感词id                                                                                                                                                                                                                                                                                                                | |

## 请求示例
```shell
curl -X POST http://localhost:8006/data/sensitive_words/offline \
-H 'Content-type: application/json' \
-d '{
   "id": 1
}'
```

## 响应参数
同 `upset接口`