# 接口文档
## 基本信息
- 接口地址：http://localhost:8005/keyword/query
- 请求方法：POST
- 请求头： Content-Type: application/json

请求参数

| 字段名          | 类型     | 是否必填 | 说明            |
|--------------| ------ |------|---------------|
| content      | string | 是    | 待识别内容         |
| businessName  | string | 是    | 业务标识，用于区分不同业务 |

示例
```json
{
  "content": "xxx王八蛋yy",
  "businessName": "abc"
}
```


## 响应

| 字段名               | 类型     | 说明            |
| ----------------- | ------ | ------------- |
| `statusCode`      | int    | 状态码（200 表示成功） |
| `hitWord`         | string | 命中的敏感词        |
| `firstClassNo`    | int    | 一级分类编号        |
| `firstClassName`  | string | 一级分类名称        |
| `secondClassNo`   | int    | 二级分类编号        |
| `secondClassName` | string | 二级分类名称        |
| `bwgLabel`        | int    | 黑白灰标记（1 表示命中） |
| `latency`         | int    | 接口耗时（毫秒）      |




# 完整示例
## 输入
```shell
curl http://localhost:8005/keyword/query \
-H 'Content-type: application/json' \
-d '{
  "content": "xxx王八蛋yyy",
  "businessName": "abc"
}'
```

## 输出
```json
{
  "statusCode": 200,
  "hitWord": "王八蛋",
  "firstClassNo": 1101,
  "firstClassName": "辱骂",
  "secondClassNo": 1,
  "secondClassName": "辱骂",
  "bwgLabel": 1,
  "latency": 0
}
```