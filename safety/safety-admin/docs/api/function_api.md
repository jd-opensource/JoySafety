# 新建防御原子能力接口（new）

## 请求方法
- 请求地址：/config/defense/manage/function/new
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段 | 类型 | 必填 | 说明                                                                                                                                                                                                                                                                                                                                                                                                                           |
| ---- | ---- | ---- |------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| name | string | 是 | 原子能力名称                                                                                                                                                                                                                                                                                                                                                                                                                       |
| group | string | 是 | 原子能力所属分组（不同分组相互隔离）                                                                                                                                                                                                                                                                                                                                                                                                           |
| type | string | 否 | 原子能力的类别，枚举值：<pre><code>     <br><br>    /\*\*<br>     \* 单标签(只会对输入给出一个分类)预测概率类型<br>     \*/<br>    single_label_pred,<br><br>    /\*\*<br>     \* 黑白名单/正则匹配类<br>     \*/<br>    keyword,<br><br>    /\*\*<br>     \* 基于知识库搜索<br>     \*/<br>    kb_search,<br><br>    /\*\*<br>     \* 基于rag回复<br>     \*/<br>    rag_answer,<br><br>    /\*\*<br>     \* 多轮检测<br>     \*/<br>    multi_turn_detect<br><br><br></pre></code> |
| confObj  | object | 是 | 不同type对应的结构不同                                                                                                                                                                                                                                                                                                                                                                                                                |


### confObj 枚举结构
```json
[
  {
    "name": "keyword",
    "group": "default",
    "desc": "敏感词识别",
    "type": "keyword",
    "timeoutMilliseconds": 100,
    "status": "online",
    "confObj": {
      "name": "敏感词服务",
      "url": "http://safety-keywords:8005/keyword/query"
    }
  },
  {
    "name": "fast20250710",
    "group": "default",
    "desc": "fast20250710",
    "type": "single_label_pred",
    "timeoutMilliseconds": 100,
    "status": "online",
    "confObj": {
      "extra": {
        "url": "http://safety-fasttext:8002/fasttext/fast20250710"
      },
      "name": "fast20250710",
      "modelType": "fasttext",
      "ignoreRiskCode": []
    }
  },
  {
    "name": "bert_prompt_injection_20250828",
    "group": "default",
    "desc": "bert_prompt_injection_20250828",
    "type": "single_label_pred",
    "timeoutMilliseconds": 300,
    "status": "online",
    "confObj": {
      "extra": {
        "url": "http://safety-bert:8003/v1/bert/bert_prompt_injection_20250828"
      },
      "name": "bert_prompt_injection_20250828",
      "modelType": "bert",
      "ignoreRiskCode": []
    }
  },
  {
    "name": "kb_search",
    "group": "default",
    "desc": "红线知识检索",
    "type": "kb_search",
    "timeoutMilliseconds": 300,
    "status": "online",
    "confObj": {
      "topK": 5,
      "threshold": 0.8,
      "collection": "red_kg_prod_hnsw_bge512_20250826",
      "url": "http://safety-knowledge:8004/knowledge/search"
    }
  },
  {
    "name": "rag_answer",
    "group": "default",
    "desc": "红线代答",
    "type": "rag_answer",
    "timeoutMilliseconds": 3000,
    "confObj": {
      "topK": 5,
      "threshold": 0.8,
      "collection": "hnsw_bge512_20250826",
      "url": "http://safety-knowledge:8004/knowledge/answer"
    }
  },
  {
    "name": "multi_turn_detect",
    "group": "default",
    "desc": "多轮对话检测",
    "type": "multi_turn_detect",
    "timeoutMilliseconds": 3000,
    "status": "online",
    "confObj": {
      "maxTurns": 20,
      "url": "http://safety-knowledge:8004/dialog"
    }
  }
]
```


## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/function/new \
-H 'Content-type: application/json' \
-d '{
    "confObj": {
        "name": "敏感词服务",
        "url": "http://safety-keywords:8005/keyword/query"
    },
    "group": "default",
    "name": "keyword",
    "type": "keyword",
    "desc": "敏感词服务",
    "timeoutMilliseconds": 200
}
```

## 响应参数

**Body(JSON)**

| 字段名     | 类型     | 说明                                                                                       |
| ------- |--------|------------------------------------------------------------------------------------------|
| code    | int    | 状态码，0 表示成功                                                                               |
| message | string | 响应信息，例如 `"success"`                                                                      |
| cost    | float  | 请求耗时，单位：秒                                                                                |
| data    | object | - 相同字段含义同输入 <br> - version代表版本号<br/> - status代表状态<br/>- id字段为自动生成的唯一id<br/>- 其它字段为预留，可忽略 |


## 结果示例
```json
{
  "code": 0,
  "message": "success",
  "cost": 0,
  "data": {
    "id": 1,
    "name": "keyword",
    "group": "default",
    "desc": "默认的敏感词服务2",
    "type": "keyword",
    "timeoutMilliseconds": 200,
    "version": 1,
    "status": "edit",
    "confObj": {
      "name": "敏感词服务",
      "url": "http://safety-keywords:8005/keyword/query"
    }
  }
}
```

# 根据id获取原子能力（get）
## 请求方法
- 请求地址：/config/defense/manage/function/get
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建原子能力时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/function/get \
-H 'Content-type: application/json' \
-d '{
    "id": 1
  }'
```

## 响应参数
同`新建原子能力接口`的返回

# 原子能力上线（online）
**描述**  
操作原子能力上线。

## 请求方法
- 请求地址：/config/defense/manage/function/online
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建原子能力时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/function/online \
-H 'Content-type: application/json' \
-d '{
    "id": 1
  }'
```

## 响应参数
同`新建原子能力接口`的返回

# 原子能力下线（offline）
**描述**  
操作原子能力下线。

## 请求方法
- 请求地址：/config/defense/manage/function/offline
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建原子能力时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/function/offline \
-H 'Content-type: application/json' \
-d '{
    "id": 1
  }'
```

## 响应参数
同`新建原子能力接口`的返回


# 已有原子能力新建版本（newVersion）
**描述**  
创建新版本原子能力配置。

## 请求方法
- 请求地址：/config/defense/manage/function/newVersion
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建原子能力时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/function/offline \
-H 'Content-type: application/json' \
-d '{
    "id": 1
  }'
```

## 响应参数
同`新建原子能力接口`的返回，返回的数据同原版本，只是id为新的，且version字段增加1


# 更新原子能力接口（update）

## 请求方法
- 请求地址：/config/defense/manage/function/update
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                                                                                                                                                |
|-------------------| ---- | ---- |---------------------------------------------------------------------------------------------------------------------------------------------------|
| id                | number | 是 | 原子能力id                                                                                                                                              |
**其它字段同新建原子能力接口**
## 请求示例
**除id字段外同新建原子能力接口**


## 响应参数
同`新建原子能力接口`的返回。

# 原子能力配置升级（upgrade）
**描述**  
升级原子能力配置到指定版本。

## 请求方法
- 请求地址：/config/defense/manage/function/upgrade
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明         |
|-------------------| ---- | ---- |------------|
| id                | string | 是 | 新版本的原子能力配置id |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/function/upgrade \
-H 'Content-type: application/json' \
-d '{
    "id": 2
  }'
```

## 响应参数
同`新建原子能力接口`的返回，返回id版本对应的配置，同时将状态为online的配置置为offline。


# 查看当前原子能力生效配置（active）
**描述**  
获取指定原子能力生效的配置。

## 请求方法
- 请求地址：/config/defense/manage/function/active
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明   |
|-------------------| ---- | ---- |------|
| group                | string | 是 | 原子能力分组 |
| name                | string | 是 | 原子能力名称 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/function/active \
-H 'Content-type: application/json' \
-d '{
    "group": "default",
    "name": "single_test"
}'
```

## 响应参数
同`新建原子能力接口`的返回。


# 查看所有（或者指定分组）所有原子能力生效配置（allActive）
## 请求方法
- 请求地址：/config/defense/manage/function/allActive
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明   |
|-------------------| ---- |----|------|
| group                | string | 否  | 原子能力分组 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/function/active \
-H 'Content-type: application/json' \
-d '{
    "group": "default",
}'
```

## 响应参数
返回原子能力列表，含义同`新建原子能力接口`的返回。
