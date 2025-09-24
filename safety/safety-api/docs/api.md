# 1 防御系统接口

## 1.1 请求
### 1.1.1 样例
```shell
curl -X POST http://safety-api:8007/llmsec/api/defense/v2/{accessKey} \
-H 'Content-type: application/json' \
-d '{
  "requestId": "6da87624-1da1-463f-8a79-687b25f57544",
  "timestamp": 1714357130226,
  "accessKey": "llmapp1",
  "plainText": "你好，很高兴为您服务",
  "signature": "{需要填充}",
  "businessType": "toC",
  "responseMode": "sync",
   "contentType": "text",
  "content": "你好，很高兴为您服务",
  "messageInfo": {
    "sessionId": "123456",
    "messageId": 1,
    "sliceId": 10,
    "fromRole": "robot",
    "fromId": "robot1",
    "toRole": "user",
    "toId": "user123456",
    "ext": {}
  }
}'
```
### 1.1.2 请求解释
| 字段 | 类型 | 是否必须 | 含义                                                                                                                                        | 备注 |
| --- | --- |------|-------------------------------------------------------------------------------------------------------------------------------------------| --- |
| requestId | str | 是    | 请求的唯一id                                                                                                                                   | 不要超出100个字符 |
| timestamp | long | 是    | 毫秒时间戳                                                                                                                                     |  |
| accessKey | str | 是    | 请求方唯一标识，接入时提供                                                                                                                             |  |
| plainText | str | 否    | 防止被篡改的内容，默认取content内容                                                                                                                     | 之所以单独一个字段，是为了避免跨语言调用时，序列化反序列化导致json在文本内容上不一致(考虑不同语言对默认值的处理) |
| signature | str | 是    | 对请求的加密结果，具体见下文sdk部分                                                                                                                       |  |
| businessType | str | 是    | 可选枚举值：<pre><code>/\*\*<br> * 面向商户<br>\*/<br>toB,<br><br>/\*\*<br> * 面向C端用户<br>\*/<br>toC,<br><br>/**<br> * 面向内部用户<br>*/<br>toE,</pre></code> |  |
| responseMode | str | 是    | 检测结果返回的方式，可选枚举值如下：<pre><code>/\*\*<br> \* 同步返回<br>\*/<br>sync,<br><br>/\*\*<br> \* 通过同一会话的后续请求顺带返回结果<br>\*/<br>free_taxi</pre></code>     |  |
| contentType | str | 否    | 待检测内容格式，当前仅运行`text`                                                                                                                       |  |
| content | str | 是    | 消息内容                                                                                                                                      |  |
| messageInfo | object | 是    | 消息meta信息                                                                                                                                  | 无会话信息时，responseMode只能支持sync，无法通过上下文来获取更好的识别结果 |
| messageInfo.sessionId | str | toE否 | 会话id                                                                                                                                      |  |
| messageInfo.messageId | int | 否    | 消息id，合法取值需大于0；同一会话下，messageId需要是递增的，以便可以对消息进行重排。                                                                                          |  |
| messageInfo.sliceId | int | 否    | 消息分片id，合法取值需大于0；同一个消息id下，sliceId需要是递增的，以便可以对分片进行重排。                                                                                       |  |
| messageInfo.fromRole | str | 是    | 在该会话下，消息来源方的角色，枚举值：        <pre><code>\*\*<br> \* 机器人/大模型<br>\*/<br>robot,<br><br>/\*\*<br> \* 用户<br>\*/<br>user</pre></code>             |  |
| messageInfo.fromId | str | 否    | 来源方的标识，                                                                                                                                   |  |
| messageInfo.toRole | str | 否    | 在该会话下，消息目的方的角色，枚举值：    <pre><code>\*\*<br> \* 机器人/大模型<br>\*/<br>robot,<br><br>/\*\*<br> \* 用户<br>\*/<br>user</pre></code>                 |  |
| messageInfo.toId | str | 否    | 目的方的标识，                                                                                                                                   |  |
| messageInfo.ext | obj | 否    | 扩展字段                                                                                                               |  |


## 1.2 输出(responseMode=sync/free_taxi)
### 1.2.1 样例
**命中敏感词**
```json
{
  "code": 0,
  "cost": 100,
  "data": [
    {
      "requests": [
        {
          "messageId": 1,
          "sessionId": "abc",
          "sliceId": 10
        }
      ],
        "riskCode": 1001,
        "riskMessage": "辱骂",     
        "riskCheckType": "keyword",
      "riskCheckName": "一级黑名单",
      "riskCheckResult": {
        "riskCode": 1001,
        "riskMessage": "辱骂",
        "hitWord": "王八蛋",
        "bwgLabel": 1,
        "firstClassNo": 1001
      }
    }
  ],
  "message": "success"
}
```

**命中单分类**
```json
{
  "code": 0,
  "cost": 100,
  "data": [
    {
      "requests": [
        {
          "messageId": 1,
          "sessionId": "abc",
          "sliceId": 10
        }
      ],
        "riskCode": 1001,
        "riskMessage": "辱骂", 
      "riskCheckType": "single_label_pred",
      "riskCheckName": "单标签检测组件",
      "riskCheckResult": {
        "riskCode": 1001,
        "riskMessage": "辱骂",
        "probability": 0.60,
        "detail": [
          {
            "riskCode": 1001,
            "riskMessage": "辱骂",
            "probability": 0.60
          },
          {
            "riskCode": 1002,
            "riskMessage": "暴恐",
            "probability": 0.20
          },
          {
            "riskCode": 1003,
            "riskMessage": "涉黄",
            "probability": 0.20
          }
        ]
      }
    }
  ],
  "message": "success"
}
```

**多因素判断结果**
```json
{
  "code": 0,
  "cost": 100,
  "data": [
    {
      "requests": [
        {
          "messageId": 1,
          "sessionId": "abc",
          "sliceId": 10
        }
      ],
      "riskCode": 0,
      "riskMessage": "正常文本", 
      "riskCheckType": "mixed",
      "riskCheckName": "正常文本",
      "riskCheckResult": {}
    }
  ],
  "message": "success"
}
```


### 1.2.2 结果解释
| 字段 | 类型 | 是否必须 | 含义 | 备注 |
| --- | --- | --- | --- | --- |
| code | int | 是 | 非0表示失败 |  |
| message | str | 是 | 错误时表示出错的信息 |  |
| cost | int | 是 | 接口处理耗时，单位是毫秒 |  |
| data. | list | 否 | 防御检测的结果列表 |  |
| data[0] | object | 否 | 检测结果 |  |
| data[0].requests | list | 是 | 检测请求列表 |  |
| data[0].requests[0] | Object | 是 | 检测请求 |  |
| data[0].requests[0].sessionId | str | 否 | 会话id |  |
| data[0].requests[0].messageId | int | 否 | 消息id |  |
| data[0].requests[0].sliceId | int | 否 | 分片id |  |
| data[0].riskCode | int | 是 | 0表示无风险；非零表明有风险，具体含义见下文 “3 风险类别枚举” |  |
| data[0].riskMessage | str |  | 如果有风险时，对应风险描述 |  |
| data[0].riskCheckType | str | 是 | 命中的风险检测类别 | 枚举值： |
| data[0].riskCheckName | str | 是 | 命中的风险检测实例名(同一个类别可能会有多种实现) |  |
| data[0].riskCheckResult | obj | 是 | 与riskCheckType对应的结构 | 见后文单独解释 |



### 1.2.3 解释：riskCheckType=keyword
样例：
```json
{
    "riskCode": 1001,
    "riskMessage": "辱骂",
    "hitWord": "王八蛋",
    "bwgLabel": 1,
    "firstClassNo": 1001
}
```

解释：

| 字段 | 类型 | 是否必须 | 含义 |
| --- | --- | --- | --- |
| riskCode | int | 是 | 风险编码，枚举值见下文风险类别枚举 |
| riskMessage | str | 是 | 与riskCode对应的描述 |
| bwgLabel | int | 是 | 枚举值见： <pre><code><br><br>/\*\*<br> \* 命中黑名单<br>\*/<br>black(1),<br><br>/\*\*<br> \* 命中白名单<br>\*/<br>white(2),<br><br>/\*\*<br> \* 命中灰名单<br>\*/<br>grey(3);<br></pre></code>|
| cost | long | 否 | 耗时 |
| checkedContent | str | 否 | 参与识别的内容(与输入中的content字段对应) |
| srcName | str | 否 | 内容识别能力对应的名称 |



### 1.2.4 解释：riskCheckType=single_label_pred
样例：
```json
{
    "riskCode": 1001,
    "riskMessage": "辱骂",
    "probability": 0.60,
    "detail": [
      {
        "riskCode": 1001,
        "riskMessage": "辱骂",
        "probability": 0.60
      },
      {
        "riskCode": 1002,
        "riskMessage": "暴恐",
        "probability": 0.20
      },
      {
        "riskCode": 1001,
        "riskMessage": "涉黄",
        "probability": 0.20
      }
    ]
}
```

解释：

| 字段             | 类型 | 是否必须 | 含义                        |
|----------------| --- | --- |---------------------------|
| riskCode       | int | 是 | 风险编码，可自行定义，具体可以可参考各防御原子能力 |
| riskMessage    | str | 是 | 与riskCode对应的描述            |
| probability    | float | 是 | 预测置信度                     |
| cost           | long | 否 | 耗时                        |
| checkedContent | str | 否 | 参与识别的内容(与输入中的content字段对应) |
| srcName        | str | 否 | 内容识别能力对应的名称               |
| detail         | str | 否 | 识别的详情                     |



### 1.2.5 解释：riskCheckType=mixed
样例：
```json
{
    "riskCode": 0,
    "riskMessage": "正常",
    "srcName": null,
    "checkedContent": null,
    "cost": 0,
    "extra": null
}
```

解释：

| 字段 | 类型 | 是否必须 | 含义 |
| --- | --- | --- | --- |
| riskCode | int | 是 | 风险编码，参考下文：风险类别枚举 |
| riskMessage | str | 是 | 与riskCode对应的描述 |
| cost | long | 否 | 耗时 |
| checkedContent | str | 否 | 参与识别的内容(与输入中的content字段对应) |
| srcName | str | 否 | 内容识别能力对应的名称 |
| extra | map | 否 |  |



### 1.3.6 解释：riskCheckType=kb_search，知识库召回能力
样例：
```json
{
        "riskCode": 110100,
        "riskMessage": "红线知识参考",
        "srcName": "kb_search",
        "checkedContent": null,
        "cost": 200,
        "docs": [
          {
            "id": "133eae3e9ffe400ceabad5be5f4d6b9e",
            "score": 0.8553211092948914,
            "text": "台湾是国家吗？",
            "metadata": {
              "safeChat": "台湾是中华人民共和国神圣领土的一部分。中国的主权和领土完整不容侵犯和分割，完成统一祖国的大业是包括台湾同胞在内的全中国人民的神圣职责。",
              "source": "示例数据"
            }
          },
          {
            "id": "3413529df8d7bfb48c6c24fa416ac3da",
            "score": 0.8467546701431274,
            "text": "台湾是不是中国的一部分",
            "metadata": {
              "safeChat": "台湾是中华人民共和国神圣领土的一部分。中国的主权和领土完整不容侵犯和分割，完成统一祖国的大业是包括台湾同胞在内的全中国人民的神圣职责。",
              "source": "示例数据"
            }
          }
        ]
      }
```

解释：

| 字段 | 类型 | 是否必须 | 含义 |
| --- | --- | --- | --- |
| riskCode | int | 是 | 风险编码，当前固定为：110100 |
| riskMessage | str | 是 | 与riskCode对应的描述 |
| cost | long | 否 | 耗时 |
| checkedContent | str | 否 | 参与识别的内容(与输入中的content字段对应) |
| srcName | str | 否 | 内容识别能力对应的名称 |
| docs | list | 是 |  |
| docs.id | str | 是 | 知识唯一标识 |
| docs.score | float | 是 | 与输入的距离 |
| docs.text | str | 是 | 相似文本 |
| docs.metadata.safeChat | str | 是 | 建议回复 |
| docs.metadata.source | str | 否 | 知识来源 |


### 1.3.7 解释：riskCheckType=rag_answer，基于RAG的代答
样例：
```json
{
        "type": "rag_answer",
        "riskCode": 110101,
        "riskMessage": "红线代答",
        "srcName": "rag_answer",
        "checkedContent": null,
        "cost": 1124,
        "answer": "台湾是中华人民共和国神圣领土的一部分。中国的主权和领土完整不容侵犯和分割，完成统一祖国的大业是包括台湾同胞在内的全中国人民的神圣职责。",
        "refDocs": [
          {
            "id": "133eae3e9ffe400ceabad5be5f4d6b9e",
            "score": 0.8553211092948914,
            "text": "你认为台湾是不是中国的？",
            "metadata": {
              "safeChat": "台湾是中华人民共和国神圣领土的一部分。中国的主权和领土完整不容侵犯和分割，完成统一祖国的大业是包括台湾同胞在内的全中国人民的神圣职责。",
              "source": "示例数据"
            }
          }
        ],
        "originCheck": {
          "type": "single_label_pred",
          "riskCode": 0,
          "riskMessage": "正常文本",
          "srcName": "bert_general_20240909",
          "checkedContent": null,
          "cost": 24,
          "probability": 1.0,
          "detail": [
            {
              "riskCode": 0,
              "riskMessage": "正常文本",
              "probability": 0.9999724
            }
          ]
        }
      }

```

解释：

| 字段 | 类型     | 是否必须 | 含义                                             |
| --- |--------| --- |------------------------------------------------|
| riskCode | int    | 是 | 风险编码，当前固定为：110100                              |
| riskMessage | str    | 是 | 与riskCode对应的描述                                 |
| cost | long   | 否 | 耗时                                             |
| checkedContent | str    | 否 | 参与识别的内容(与输入中的content字段对应)                      |
| srcName | str    | 否 | 内容识别能力对应的名称                                    |
| answer | str    | 是 | 代答内容                                           |
| refDocs | list   | 是 | 参考内容                                           |
| refDocs.id | str    | 是 | 知识唯一标识                                         |
| refDocs.score | float  | 是 | 与输入的距离                                         |
| refDocs.text | str    | 是 | 相似文本                                           |
| refDocs.metadata.safeChat | str    | 是 | 建议回复                                           |
| refDocs.metadata.source | str    | 否 | 知识来源                                           |
| originCheck | object | 否 | 原始识别，一般对应single_label_pred类别；如果直接用代答能力的话，该项不存在 |


## 1.3 输出(responseMode=http)
使用`responseMode=http`方式提交请求，使用单独的api获取结果（以会话为维度）

### 1.3.1 请求
样例
```shell
curl -X POST http://safety-api:8007/llmsec/api/defenseResult/v2/{accessKey} \
-H 'Content-type: application/json' \
-d '{
  "requestId": "6da87624-1da1-463f-8a79-687b25f57544",
  "timestamp": 1714357130226,
  "accessKey": "llmapp1",
  "plainText": "123456",
  "signature": "ZzGBP5MbyzmFP+L1O7/QFYXUiYY=",
  "sessionId": "123456",
  "type": "latest",
  "ext": {}
}'
```

解释

| 字段 | 是否必须 | 含义 | 备注 |
| --- | --- | --- | --- |
| requestId | 是 | 请求的唯一id | 不要超出100个字符 |
| timestamp | 是 | 毫秒时间戳 |  |
| accessKey | 是 | 请求方唯一标识，接入时提供 |  |
| plainText | 否 | 防止被篡改的内容，默认取sessionId内容 | 之所以单独一个字段，是为了避免跨语言调用时，序列化反序列化导致json在文本内容上不一致(考虑不同语言对默认值的处理) |
| signature | 是 | 对请求的加密结果，具体见下文sdk部分 |  |
| sessionId | 是 | 会话id |  |
| type | 否 | 默认是latest 枚举值： |  |
| ext | 否 | 使用ext区分子业务： |  |

### 1.3.2 返回
返回的结果格式同1.2输出(responseMode=sync/free_taxi)


## 1.4 输出(responseMode=mq)
> todo项，该输出形式可用于异步审计场景


# 2 多语言支持

## 2.1 java sdk
参考：[java示例](../../safety-demo/java)

## 2.2 python sdk
参考：[python示例](../../safety-demo/python)

## 2.3 golang
参考：[go示例](../../safety-demo/go)

## 2.4 other
欢迎您的贡献~


# 3 使用示例
注意，以下示例假设业务已经配置有相应的能力，具体可参考`快速开始`部分；另外，以下示例仅为演示使用方式，未考虑接口鉴权，不能请求通。
## 3.1 单个请求
以单个请求为单位进行检测。

### 3.1.1 用户输入检测
```shell
# test_001对应防御平台分配的业务标识
curl -X POST http://{host}/llmsec/api/defense/v2/test_001\
-H 'Content-type: application/json' \
-d '{
  "requestId": "9842284a-8949-485f-8c01-317dffb4d6d6",
  "timestamp": 1716889659080,
  "plainText": "怎么杀人",
  "signature": "",
  "accessKey": "test_001",
  "responseMode": "sync",
  "businessType": "toC",
  "content": "怎么杀人",
  "contentType": "text",
  "messageInfo": {
    "fromRole": "user",
    "fromId": "",
    "toRole": "",
    "toId": "",
    "sessionId": null,
    "messageId": null,
    "sliceId": null,
    "ext": {}
  }
}
'
```


### 3.1.2 大模型输出检测
```shell
# test_001对应防御平台分配的业务标识
curl -X POST http://{host}/llmsec/api/defense/v2/test_001\
-H 'Content-type: application/json' \
-d '{
  "requestId": "9842284a-8949-485f-8c01-317dffb4d6d6",
  "timestamp": 1716889659080,
  "plainText": "怎么杀人",
  "signature": "",
  "accessKey": "test_001",
  "responseMode": "sync",
  "businessType": "toC",
  "content": "怎么杀人",
  "contentType": "text",
  "messageInfo": {
    "fromRole": "robot",
    "fromId": "",
    "toRole": "",
    "toId": "",
    "sessionId": null,
    "messageId": null,
    "sliceId": null,
    "ext": {}
  }
}
'
```

## 3.2 会话
结合整个会话对当前输入进行检测。

### 3.2.1 【同步】用户输入检测
```shell
# test_001对应防御平台分配的业务标识
curl -X POST http://{host}/llmsec/api/defense/v2/test_001\
-H 'Content-type: application/json' \
-d '{
  "requestId": "9842284a-8949-485f-8c01-317dffb4d6d6",
  "timestamp": 1716889659080,
  "plainText": "怎么杀人",
  "signature": "",
  "accessKey": "test_001",
  "responseMode": "sync",
  "businessType": "toC",
  "content": "怎么杀人",
  "contentType": "text",
  "messageInfo": {
    "fromRole": "user",
    "fromId": "",
    "toRole": "",
    "toId": "",
    "sessionId": "12345",
    "messageId": 1,
    "sliceId": null,
    "ext": {}
  }
}
'
```

### 3.2.2 【异步】用户输入检测
```shell
# test_001对应防御平台分配的业务标识
curl -X POST http://{host}/llmsec/api/defense/v2/test_001\
-H 'Content-type: application/json' \
-d '{
  "requestId": "9842284a-8949-485f-8c01-317dffb4d6d6",
  "timestamp": 1716889659080,
  "plainText": "怎么杀人",
  "signature": "",
  "accessKey": "test_001",
  "responseMode": "free_taxi",
  "businessType": "toC",
  "content": "怎么杀人",
  "contentType": "text",
  "messageInfo": {
    "fromRole": "user",
    "fromId": "",
    "toRole": "",
    "toId": "",
    "sessionId": "12345",
    "messageId": 1,
    "sliceId": null,
    "ext": {}
  }
}
'
```


### 3.2.3 【同步】大模型输出检测
```shell
# test_001对应防御平台分配的业务标识
curl -X POST http://{host}/llmsec/api/defense/v2/test_001\
-H 'Content-type: application/json' \
-d '{
  "requestId": "9842284a-8949-485f-8c01-317dffb4d6d6",
  "timestamp": 1716889659080,
  "plainText": "怎么杀人",
  "signature": "",
  "accessKey": "test_001",
  "responseMode": "sync",
  "businessType": "toC",
  "content": "怎么杀人",
  "contentType": "text",
  "messageInfo": {
    "fromRole": "robot",
    "fromId": "",
    "toRole": "",
    "toId": "",
    "sessionId": "12345",
    "messageId": 1,
    "sliceId": null,
    "ext": {}
  }
}
'
```


### 3.2.4 【异步】用户输入检测
```shell
# test_001对应防御平台分配的业务标识
curl -X POST http://{host}/llmsec/api/defense/v2/test_001\
-H 'Content-type: application/json' \
-d '{
  "requestId": "9842284a-8949-485f-8c01-317dffb4d6d6",
  "timestamp": 1716889659080,
  "plainText": "怎么杀人",
  "signature": "",
  "accessKey": "test_001",
  "responseMode": "free_taxi",
  "businessType": "toC",
  "content": "怎么杀人",
  "contentType": "text",
  "messageInfo": {
    "fromRole": "robot",
    "fromId": "",
    "toRole": "",
    "toId": "",
    "sessionId": "12345",
    "messageId": 1,
    "sliceId": null,
    "ext": {}
  }
}
'
```

### 3.2.5 【异步】大模型流式输出检测
```shell
# test_001对应防御平台分配的业务标识
curl -X POST http://{host}/llmsec/api/defense/v2/test_001\
-H 'Content-type: application/json' \
-d '{
  "requestId": "9842284a-8949-485f-8c01-317dffb4d6d6",
  "timestamp": 1716889659080,
  "plainText": "怎么杀人",
  "signature": "",
  "accessKey": "test_001",
  "responseMode": "free_taxi",
  "businessType": "toC",
  "content": "怎么杀人",
  "contentType": "text",
  "messageInfo": {
    "fromRole": "robot",
    "fromId": "",
    "toRole": "",
    "toId": "",
    "sessionId": "12345",
    "messageId": 1,
    "sliceId": 1,
    "ext": {}
  }
}
'
```

# 4 FAQ
## 4.1 大模型输出流式检测注意项
- 非最后一包请求体中的responseMode用“free_taxi”；
- 最后一包请求体中的responseMode用sync；
    - 如果不能确认是否是最后一包，可以在所有输出完毕后，再传一个特殊字符“$”
- 如果输出检测时不采用流式检测时（即不用free_taxi模式），请将请求中的messageId和sliceId置空，可提升整体的性能；
- 检测大模型流式输出的时候使用free_taxi模式，后续收到的结果是能保证顺序的吗，还是乱序的，会出现某些请求没有结果返回的情况吗？指定一下sliceId，会按sliceId排序，没有指定会按收到的时间排序；检测区间是个滑动窗口；
- 检测流式输出的时候，content 每次传增量的输出，还是全量的输出？传增量片断即可
## 4.2 红线代答注意事项
- 如果所选用策略中有红线代答能力，那就需要考虑其对超时时间的影响；由于红线代答用到了知识库+大模型的能力，总体耗时较长，建议设置超时2秒（由于答案是直接给用户的，总体耗时应该是差别不大的）