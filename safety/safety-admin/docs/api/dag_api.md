# 新建dag接口（newDagWithYaml）

## 请求方法
- todo 功能验证
- 请求地址：/config/defense/manage/dag/newDagWithYaml
- 请求方法：POST
- 请求头：Content-Type: text/plain

## 请求参数（yaml Body）

| 字段           | 类型     | 必填 | 说明               |
|--------------|--------|----|------------------|
| businessName | string | 是  | 对应的业务名称          |
| group        | string | 是  | 所属业务分组（不同分组相互隔离） |
| desc         | string | 是  | 描述               |
| rootId      | string | 是  | dag的根结点          |
| confArray      | object | 是  | dag的结点列表         |

### confArray

| 字段        | 类型     | 必填 | 说明                                |
|-----------|--------|----|-----------------------------------|
| nodeId    | string | 是  | 结点id                              |
| functionConf     | object | 是  | 原子能力配置                            |
| routerConf      | object | 是  | router用于确定下一个结点（如果是最后一个结点，需要有返回值） |
| ignoreError    | bool   | 是  | 是否忽略当前结点的异常                       |

### functionConf枚举
| 字段                  | 类型     | 必填 | 说明                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|---------------------|--------|----|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| type                | string | 是  | 原子能力类别，枚举值：<pre><code> <br>   /\*\*<br>     \* 空类型，啥也不做(内部使用，一般不会返回给用户)<br>     \*/<br>    dummy,<br><br>    /\*\*<br>     \* 单标签(只会对输入给出一个分类)预测概率类型<br>     \*/<br>    single_label_pred,<br><br>    /\*\*<br>     \* 黑白名单/正则匹配类<br>     \*/<br>    keyword,<br><br>    /\*\*<br>     \* 基于知识库搜索<br>     \*/<br>    kb_search,<br><br>    /\*\*<br>     \* 基于rag回复<br>     \*/<br>    rag_answer,<br><br>    /\*\*<br>     \* 多轮检测<br>     \*/<br>    multi_turn_detect,<br><br>    /\*\*<br>     \* 并行执行的check类型(内部使用，一般不会返回给用户)<br>     \*/<br>    parallel<br><br></pre></code> |
| name                | object | 是  | 原子能力配置                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| timeoutMilliseconds | object | 是  | router用于确定下一个结点（如果是最后一个结点，需要有返回值）                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| conf                | bool   | 是  | 是否忽略当前结点的异常                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |

> todo 各类function配置样例

### routerConf
| 字段                  | 类型     | 必填 | 说明                                                                                                                                                                                                                                                                                      |
|---------------------|--------|----|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| type                | string | 是  | router类别，枚举值：<pre><code> <br><br>    groovy, // groovy脚本来判断<br>    keyword, // keyword类别<br>    user_end, // 输入如果是用户即中止（不常用）<br>    robot_end, // 输入如果是大模型即中止（不常用）<br>    stupid_end, // 无脑中止<br>    simple_next, // 无脑下一个指定指点（不常用）<br>    risk_end // 如果判断出风险即返回（已废弃）<br></pre></code> |
| name                | object | 是  | router名称                                                                                                                                                                                                                                                                                |
| conf                | bool   | 是  | router配置，不同类别不一样                                                                                                                                                                                                                                                                        |

> todo 各类router配置样例

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/dag/newDagWithYaml \
-H 'Content-type: text/plain' \
-d '
businessName: test
group: default
desc: test
rootId: start
confArray:
  - nodeId: start
    functionConf:
      type: dummy
      name: dummy
      timeoutMilliseconds: 5
      conf: {}
    routerConf:
      type: groovy
      name: groovy
      conf:
        script: |-
          def from_robot=ctx.curReq[0].fromRobot()
          if (from_robot) {
              return 'from_robot_test1'
          } else {
              return 'from_user_test1'
          }
    ignoreError: true
  - nodeId: from_user_test1
    functionConf:
      type: parallel
      name: parallel_from_user_test1
      timeoutMilliseconds: 200
      conf:
        functionConfs:
          - ref: keyword
          - ref: fast20250710
            conf:
              ignoreRiskCode:
                - 1006
    routerConf:
      type: groovy
      name: groovy
      conf:
        script: |-
          import com.jd.security.llmsec.core.check.MixedCheck
          def retMap=ctx.curResult.resultMap
          def next="from_user_test_bert2"
          def kw = retMap.keyword
          def fast = retMap.fast20250710
          
          if (kw != null && (kw.bwgLabel == 1 || kw.bwgLabel == 2 )){
              ctx.curResult = kw
              return null
          } else if (kw != null && kw.bwgLabel == 3){
              return next
          } else if (fast != null) {
              if (!fast.hasRisk() && fast.probability > 0.9) {
                ctx.curResult = fast
                return null
              } else {
                return next
              }
          }
          if (kw == null || fast == null) {
            ctx.curResult = MixedCheck.noRisk()
            return null
          }
          return next
    ignoreError: true
  - nodeId: from_user_test_bert2
    functionConf:
      type: parallel
      name: parallel_from_user_test_bert2
      timeoutMilliseconds: 1000
      conf:
        functionConfs:
          - ref: bert_20250916
            conf:
              ignoreRiskCode:
                - 1006
          - ref: bert_prompt_injection_20250828
            conf:
              ignoreRiskCode:
                - 1006
    routerConf:
      type: groovy
      name: groovy
      conf:
        script: |-
          import com.jd.security.llmsec.core.check.MixedCheck
            def retMap=ctx.curResult.resultMap
            def bert_common = retMap.bert_20250916
            def bert_inject = retMap.bert_prompt_injection_20250828
            if ((bert_common == null || (bert_common != null && !bert_common.hasRisk())) && (bert_inject == null || (bert_inject != null && !bert_inject.hasRisk()))) {
            ctx.curResult = MixedCheck.noRisk()
            return null
          }
          def thresholds_common = [0:0.9, 201:1.0, 1001:0.9, 1002:0.9, 1003:0.85, 1004:0.996, 1005:0.97, 1006:0.95, 1007:0.9, 2001:0.9, 3002:0.99, 4002:0.95, 5001:0.9, 5002:0.9]
          def thresholds_inject = [0:0.8, 201:0.99]
          
          if (bert_common.hasRisk() && bert_common.probability >= thresholds_common[bert_common.riskCode]) {
            ctx.curResult = bert_common
          } else if(bert_common.detail.find { it.riskCode == 0 }?.probability <0.1 && bert_common.riskCode != 201) {
            ctx.curResult = bert_common
          }else if(bert_inject.probability >= thresholds_inject[bert_inject.riskCode]){
            ctx.curResult = bert_inject
          } else {
            ctx.curResult = MixedCheck.noRisk()
          }
          return null
    ignoreError: true
  - nodeId: from_robot_test1
    functionConf:
      ref: keyword
    routerConf:
      type: stupid_end
      name: stupid_end
    ignoreError: true
'
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
    "id": 5,
    "businessName": "test",
    "group": "default",
    "desc": "test",
    "rootId": "start",
    "version": 1,
    "status": "edit",
    "createTime": "2025-09-17 04:39:13",
    "updateTime": "2025-09-17 04:39:13",
    "conf": "[{\"functionConf\":{\"conf\":{},\"name\":\"dummy\",\"timeoutMilliseconds\":5,\"type\":\"dummy\"},\"ignoreError\":true,\"nodeId\":\"start\",\"routerConf\":{\"conf\":{\"script\":\"def from_robot=ctx.curReq[0].fromRobot()\\nif (from_robot) {\\n    return from_robot_test1\\n} else {\\n    return from_user_test1\\n}\"},\"name\":\"groovy\",\"type\":\"groovy\"}},{\"functionConf\":{\"conf\":{\"functionConfs\":[{\"ref\":\"keyword\"},{\"ref\":\"fast20250710\",\"conf\":{\"ignoreRiskCode\":[1006]}}]},\"name\":\"parallel_from_user_test1\",\"timeoutMilliseconds\":200,\"type\":\"parallel\"},\"ignoreError\":true,\"nodeId\":\"from_user_test1\",\"routerConf\":{\"conf\":{\"script\":\"import com.jd.security.llmsec.core.check.MixedCheck\\ndef retMap=ctx.curResult.resultMap\\ndef next=\\\"from_user_test_bert2\\\"\\ndef kw = retMap.keyword\\ndef fast = retMap.fast20250710\\n\\nif (kw != null && (kw.bwgLabel == 1 || kw.bwgLabel == 2 )){\\n    ctx.curResult = kw\\n    return null\\n} else if (kw != null && kw.bwgLabel == 3){\\n    return next\\n} else if (fast != null) {\\n    if (!fast.hasRisk() && fast.probability > 0.9) {\\n      ctx.curResult = fast\\n      return null\\n    } else {\\n      return next\\n    }\\n}\\nif (kw == null || fast == null) {\\n  ctx.curResult = MixedCheck.noRisk()\\n  return null\\n}\\nreturn next\"},\"name\":\"groovy\",\"type\":\"groovy\"}},{\"functionConf\":{\"conf\":{\"functionConfs\":[{\"ref\":\"bert_20250916\",\"conf\":{\"ignoreRiskCode\":[1006]}},{\"ref\":\"bert_prompt_injection_20250828\",\"conf\":{\"ignoreRiskCode\":[1006]}}]},\"name\":\"parallel_from_user_test_bert2\",\"timeoutMilliseconds\":1000,\"type\":\"parallel\"},\"ignoreError\":true,\"nodeId\":\"from_user_test_bert2\",\"routerConf\":{\"conf\":{\"script\":\"import com.jd.security.llmsec.core.check.MixedCheck\\n  def retMap=ctx.curResult.resultMap\\n  def bert_common = retMap.bert_20250916\\n  def bert_inject = retMap.bert_prompt_injection_20250828\\n  if ((bert_common == null || (bert_common != null && !bert_common.hasRisk())) && (bert_inject == null || (bert_inject != null && !bert_inject.hasRisk()))) {\\n  ctx.curResult = MixedCheck.noRisk()\\n  return null\\n}\\ndef thresholds_common = [0:0.9, 201:1.0, 1001:0.9, 1002:0.9, 1003:0.85, 1004:0.996, 1005:0.97, 1006:0.95, 1007:0.9, 2001:0.9, 3002:0.99, 4002:0.95, 5001:0.9, 5002:0.9]\\ndef thresholds_inject = [0:0.8, 201:0.99]\\n\\nif (bert_common.hasRisk() && bert_common.probability >= thresholds_common[bert_common.riskCode]) {\\n  ctx.curResult = bert_common\\n} else if(bert_common.detail.find { it.riskCode == 0 }?.probability <0.1 && bert_common.riskCode != 201) {\\n  ctx.curResult = bert_common\\n}else if(bert_inject.probability >= thresholds_inject[bert_inject.riskCode]){\\n  ctx.curResult = bert_inject\\n} else {\\n  ctx.curResult = MixedCheck.noRisk()\\n}\\nreturn null\"},\"name\":\"groovy\",\"type\":\"groovy\"}},{\"functionConf\":{\"ref\":\"keyword\"},\"ignoreError\":true,\"nodeId\":\"from_robot_test1\",\"routerConf\":{\"name\":\"stupid_end\",\"type\":\"stupid_end\"}}]",
    "confArray": [
      {
        "nodeId": "start",
        "functionConf": {
          "type": "dummy",
          "name": "dummy",
          "timeoutMilliseconds": 5,
          "conf": {}
        },
        "routerConf": {
          "type": "groovy",
          "name": "groovy",
          "conf": {
            "script": "def from_robot=ctx.curReq[0].fromRobot()\nif (from_robot) {\n    return from_robot_test1\n} else {\n    return from_user_test1\n}"
          }
        },
        "ignoreError": true
      },
      {
        "nodeId": "from_user_test1",
        "functionConf": {
          "type": "parallel",
          "name": "parallel_from_user_test1",
          "timeoutMilliseconds": 200,
          "conf": {
            "functionConfs": [
              {
                "ref": "keyword"
              },
              {
                "ref": "fast20250710",
                "conf": {
                  "ignoreRiskCode": [
                    1006
                  ]
                }
              }
            ]
          }
        },
        "routerConf": {
          "type": "groovy",
          "name": "groovy",
          "conf": {
            "script": "import com.jd.security.llmsec.core.check.MixedCheck\ndef retMap=ctx.curResult.resultMap\ndef next=\"from_user_test_bert2\"\ndef kw = retMap.keyword\ndef fast = retMap.fast20250710\n\nif (kw != null && (kw.bwgLabel == 1 || kw.bwgLabel == 2 )){\n    ctx.curResult = kw\n    return null\n} else if (kw != null && kw.bwgLabel == 3){\n    return next\n} else if (fast != null) {\n    if (!fast.hasRisk() && fast.probability > 0.9) {\n      ctx.curResult = fast\n      return null\n    } else {\n      return next\n    }\n}\nif (kw == null || fast == null) {\n  ctx.curResult = MixedCheck.noRisk()\n  return null\n}\nreturn next"
          }
        },
        "ignoreError": true
      },
      {
        "nodeId": "from_user_test_bert2",
        "functionConf": {
          "type": "parallel",
          "name": "parallel_from_user_test_bert2",
          "timeoutMilliseconds": 1000,
          "conf": {
            "functionConfs": [
              {
                "ref": "bert_20250916",
                "conf": {
                  "ignoreRiskCode": [
                    1006
                  ]
                }
              },
              {
                "ref": "bert_prompt_injection_20250828",
                "conf": {
                  "ignoreRiskCode": [
                    1006
                  ]
                }
              }
            ]
          }
        },
        "routerConf": {
          "type": "groovy",
          "name": "groovy",
          "conf": {
            "script": "import com.jd.security.llmsec.core.check.MixedCheck\n  def retMap=ctx.curResult.resultMap\n  def bert_common = retMap.bert_20250916\n  def bert_inject = retMap.bert_prompt_injection_20250828\n  if ((bert_common == null || (bert_common != null && !bert_common.hasRisk())) && (bert_inject == null || (bert_inject != null && !bert_inject.hasRisk()))) {\n  ctx.curResult = MixedCheck.noRisk()\n  return null\n}\ndef thresholds_common = [0:0.9, 201:1.0, 1001:0.9, 1002:0.9, 1003:0.85, 1004:0.996, 1005:0.97, 1006:0.95, 1007:0.9, 2001:0.9, 3002:0.99, 4002:0.95, 5001:0.9, 5002:0.9]\ndef thresholds_inject = [0:0.8, 201:0.99]\n\nif (bert_common.hasRisk() && bert_common.probability >= thresholds_common[bert_common.riskCode]) {\n  ctx.curResult = bert_common\n} else if(bert_common.detail.find { it.riskCode == 0 }?.probability <0.1 && bert_common.riskCode != 201) {\n  ctx.curResult = bert_common\n}else if(bert_inject.probability >= thresholds_inject[bert_inject.riskCode]){\n  ctx.curResult = bert_inject\n} else {\n  ctx.curResult = MixedCheck.noRisk()\n}\nreturn null"
          }
        },
        "ignoreError": true
      },
      {
        "nodeId": "from_robot_test1",
        "functionConf": {
          "ref": "keyword"
        },
        "routerConf": {
          "type": "stupid_end",
          "name": "stupid_end"
        },
        "ignoreError": true
      }
    ]
  }
}
```

# 新建dag接口（new）

## 请求方法
- todo 功能验证
- 请求地址：/config/defense/manage/dag/new
- 请求方法：POST
- 请求头：Content-Type: application/json

> 其它同`newDagWithYaml`

# 根据id获取dag（get）
## 请求方法
- 请求地址：/config/defense/manage/dag/get
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建dag时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/dag/get \
-H 'Content-type: application/json' \
-d '{
    "id": 1
  }'
```

## 响应参数
同`新建dag接口`的返回

# dag上线（online）
**描述**  
操作dag上线。

## 请求方法
- 请求地址：/config/defense/manage/dag/online
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建dag时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/dag/online \
-H 'Content-type: application/json' \
-d '{
    "id": 1
  }'
```

## 响应参数
同`新建dag接口`的返回

# dag下线（offline）
**描述**  
操作dag下线。

## 请求方法
- 请求地址：/config/defense/manage/dag/offline
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建dag时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/dag/offline \
-H 'Content-type: application/json' \
-d '{
    "id": 1
  }'
```

## 响应参数
同`新建dag接口`的返回


# 已有dag新建版本（newVersion）
**描述**  
创建新版本dag配置。

## 请求方法
- 请求地址：/config/defense/manage/dag/newVersion
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                |
|-------------------| ---- | ---- |-------------------|
| id                | string | 是 | 含义同新建dag时接口返回的id字段 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/dag/offline \
-H 'Content-type: application/json' \
-d '{
    "id": 1
  }'
```

## 响应参数
同`新建dag接口`的返回，返回的数据同原版本，只是id为新的，且version字段增加1


# 更新dag接口（dagUpdateYaml）

## 请求方法
- 请求地址：/config/defense/manage/dag/dagUpdateYaml
- 请求方法：POST
- 请求头：Content-Type: text/plain

## 请求参数（yaml Body）

| 字段                | 类型 | 必填 | 说明                                                                                                                                                |
|-------------------| ---- | ---- |---------------------------------------------------------------------------------------------------------------------------------------------------|
| id                | number | 是 | dagid                                                                                                                                              |
**其它字段同新建dag接口（new）**
## 请求示例
**除id字段外同新建dag接口**

# 更新dag接口（update）

## 请求方法
- 请求地址：/config/defense/manage/dag/update
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明                                                                                                                                                |
|-------------------| ---- | ---- |---------------------------------------------------------------------------------------------------------------------------------------------------|
| id                | number | 是 | dagid                                                                                                                                              |
**其它字段同新建dag接口（new）**
## 请求示例
**除id字段外同新建dag接口**


## 响应参数
同`新建dag接口`的返回。

# dag配置升级（upgrade）

## 请求方法
- 请求地址：/config/defense/manage/dag/upgrade
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明         |
|-------------------| ---- | ---- |------------|
| id                | string | 是 | 新版本的dag配置id |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/dag/upgrade \
-H 'Content-type: application/json' \
-d '{
    "id": 2
  }'
```

## 响应参数
同`新建dag接口`的返回，返回id版本对应的配置，同时将状态为online的配置置为offline。


# 查看当前dag生效配置（active）
**描述**  
获取指定dag生效的配置。

## 请求方法
- 请求地址：/config/defense/manage/dag/active
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明   |
|-------------------| ---- | ---- |------|
| group                | string | 是 | dag分组 |
| name                | string | 是 | dag名称 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/dag/active \
-H 'Content-type: application/json' \
-d '{
    "group": "default",
    "name": "single_test"
}'
```

## 响应参数
同`新建dag接口`的返回。

# 查看当前dag生效配置（activeYaml）
**描述**  
获取指定dag生效的配置。

## 请求方法
- 请求地址：/config/defense/manage/dag/activeYaml
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明   |
|-------------------| ---- | ---- |------|
| group                | string | 是 | dag分组 |
| name                | string | 是 | dag名称 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/dag/activeYaml \
-H 'Content-type: application/json' \
-d '{
    "group": "default",
    "name": "single_test"
}'
```

## 响应参数
同`新建dag接口`的返回对应的yaml格式


# 查看所有（或者指定分组）所有dag生效配置（allActive）
## 请求方法
- 请求地址：/config/defense/manage/dag/allActive
- 请求方法：POST
- 请求头：Content-Type: application/json

## 请求参数（JSON Body）

| 字段                | 类型 | 必填 | 说明   |
|-------------------| ---- |----|------|
| group                | string | 否  | dag分组 |

## 请求示例
```shell
curl -X POST http://localhost:8006/config/defense/manage/dag/active \
-H 'Content-type: application/json' \
-d '{
    "group": "default",
}'
```

## 响应参数
返回dag列表，含义同`新建dag接口`的返回。
