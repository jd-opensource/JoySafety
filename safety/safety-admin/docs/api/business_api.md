# 新建业务接口（new）
**描述**  
创建新的业务。

## 请求方法
- 请求地址：/config/defense/manage/business/new
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段 | 类型 | 必填 | 说明                                                                                                                                                |
| ---- | ---- | ---- |---------------------------------------------------------------------------------------------------------------------------------------------------|
| name | string | 是 | 业务名称                                                                                                                                              |
| group | string | 是 | 业务所属分组                                                                                                                                            |
| desc | string | 否 | 描述信息                                                                                                                                              |
| type | string | 是 | 业务类别类型，枚举值：<pre><code>/\*\*<br> * 面向商户<br>\*/<br>toB,<br><br>/\*\*<br> * 面向C端用户<br>\*/<br>toC,<br><br>/**<br> * 面向内部用户<br>*/<br>toE,</pre></code> |
| secretKey | string | 是 | 业务的密钥，用于safety-api接口请求                                                                                                                             |
| qpsLimit | number | 是 | QPS 限制                                                                                                                                            |
| robotCheckConfObj | object | 是 | 机器人检查配置                                                                                                                                           |
| userCheckConfObj  | object | 是 | 用户检查配置                                                                                                                                            |

### robotCheckConfObj

| 字段 | 类型 | 必填 | 说明                                            |
| ---- | ---- | ---- |-----------------------------------------------|
| checkNum | number | 是 | 上下文检测窗口大小                                     |
| unit | string | 是 | 检查单位，如 `slice_single`表示单分片，`slice_multi`表示多分片 |
| timeoutMilliseconds | number | 是 | 超时时间（毫秒）                                      |

### userCheckConfObj

| 字段 | 类型 | 必填 | 说明                                                         |
| ---- | ---- | ---- |------------------------------------------------------------|
| checkNum | number | 是 | 上下文检测窗口大小                                                  |
| unit | string | 是 | 检查单位，如 `message_single`表示用户的单条消息， `message_multi`表示用户的多条消息 |
| timeoutMilliseconds | number | 是 | 超时时间（毫秒）                                                   |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/business/new \
-H 'Content-type: application/json' \
-d '{
    "name": "test",
    "group": "default",
    "desc": "测试key",
    "type": "toC",
    "secretKey": "4ab8036c-5277-4732-a294-b9575a3bad35",
    "qpsLimit": 20,
    "robotCheckConfObj": {
      "checkNum": 1,
      "unit": "slice_single",
      "timeoutMilliseconds": 5000000
    },
    "userCheckConfObj": {
      "checkNum": 1,
      "unit": "message_single",
      "timeoutMilliseconds": 5000000
    }
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
    "name": "test",
    "group": "default",
    "desc": "测试key",
    "type": "toC",
    "secretKey": "4ab8036c-5277-4732-a294-b9575a3bad35",
    "qpsLimit": 20,
    "version": -1,
    "status": "edit",
    "accessTargets": "[\"defenseV2\",\"defenseResultV2\"]",
    "robotCheckConf": "{\"adaptivePauseMilliseconds\":50,\"checkNum\":1,\"timeoutMilliseconds\":5000000,\"unit\":\"message_single\"}",
    "userCheckConf": "{\"adaptivePauseMilliseconds\":50,\"checkNum\":1,\"timeoutMilliseconds\":5000000,\"unit\":\"message_single\"}",
    "accessTargetsArray": [
      "defenseV2",
      "defenseResultV2"
    ],
    "robotCheckConfObj": {
      "unit": "message_single",
      "checkNum": 1,
      "timeoutMilliseconds": 5000000,
      "adaptivePauseMilliseconds": 50
    },
    "userCheckConfObj": {
      "unit": "message_single",
      "checkNum": 1,
      "timeoutMilliseconds": 5000000,
      "adaptivePauseMilliseconds": 50
    }
  }
}
```

# 根据id获取业务（get）
**描述**  
操作业务上线。

## 请求方法
- 请求地址：/config/defense/manage/business/get
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建业务时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/business/get \
-H 'Content-type: application/json' \
-d '{
    "id": 1
  }'
```

## 响应参数
同`新建业务接口`的返回

# 业务上线（online）
**描述**  
操作业务上线。

## 请求方法
- 请求地址：/config/defense/manage/business/online
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建业务时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/business/online \
-H 'Content-type: application/json' \
-d '{
    "id": 31
  }'
```

## 响应参数
同`新建业务接口`的返回

# 业务下线（offline）
**描述**  
操作业务下线。

## 请求方法
- 请求地址：/config/defense/manage/business/offline
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建业务时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/business/offline \
-H 'Content-type: application/json' \
-d '{
    "id": 1
  }'
```

## 响应参数
同`新建业务接口`的返回


# 已有业务新建版本（newVersion）
**描述**  
创建新版本业务配置。

## 请求方法
- 请求地址：/config/defense/manage/business/newVersion
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建业务时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/business/offline \
-H 'Content-type: application/json' \
-d '{
    "id": 1
  }'
```

## 响应参数
同`新建业务接口`的返回，返回的数据同原版本，只是id为新的，且version字段增加1


# 更新业务接口（update）
**描述**  
创建一个新的访问 Key。

## 请求方法
- 请求地址：/config/defense/manage/business/update
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）
| 字段                | 类型 | 必填 | 说明                                                                                                                                                |
|-------------------| ---- | ---- |---------------------------------------------------------------------------------------------------------------------------------------------------|
| id                | number | 是 | 业务id                                                                                                                                              |
**其它字段同新建业务接口**
## 请求示例
**除id字段外同新建业务接口**


## 响应参数
同`新建业务接口`的返回。

# 业务配置升级（upgrade）
**描述**  
升级业务配置到指定版本。

## 请求方法
- 请求地址：/config/defense/manage/business/upgrade
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明         |
|-------------------| ---- | ---- |------------|
| id                | string | 是 | 新版本的业务配置id |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/business/upgrade \
-H 'Content-type: application/json' \
-d '{
    "id": 2
  }'
```

## 响应参数
同`新建业务接口`的返回，返回id版本对应的配置，同时将状态为online的配置置为offline。


# 查看当前业务生效配置（active）
**描述**  
获取指定业务生效的配置。

## 请求方法
- 请求地址：/config/defense/manage/business/active
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明   |
|-------------------| ---- | ---- |------|
| group                | string | 是 | 业务分组 |
| name                | string | 是 | 业务名称 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/business/active \
-H 'Content-type: application/json' \
-d '{
    "group": "default",
    "name": "single_test"
}'
```

## 响应参数
同`新建业务接口`的返回。


# 查看所有（或者指定分组）所有业务生效配置（allActive）
## 请求方法
- 请求地址：/config/defense/manage/business/allActive
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明   |
|-------------------| ---- |----|------|
| group                | string | 否  | 业务分组 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/business/active \
-H 'Content-type: application/json' \
-d '{
    "group": "default",
}'
```

## 响应参数
返回业务列表，含义同`新建业务接口`的返回。
