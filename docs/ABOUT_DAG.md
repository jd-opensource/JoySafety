# JoySafety策略配置（DAG）详解


## 概述

### 什么是策略配置DAG？

JoySafety采用 **有向无环图（DAG）** 来编排安全检测策略，每个策略从`rootId`对应的节点开始，逐层执行识别策略。这种设计具有以下优势：

- **灵活性**：支持复杂的条件分支
- **高性能**：支持并行处理
- **可维护性**：策略配置和代码分离，易于管理

### 策略执行流程

```
请求 → API服务 → 加载策略配置 → 构建DAG → 执行节点链 → 返回结果
```

每个节点包含两个核心部分：
- **Function**：当前节点执行的具体能力
- **Router**：基于当前节点的识别结果决定下一个节点

---

## DAG系统架构

### 核心组件架构图

```
┌─────────────────────────────────────────────────────────┐
│                    JoySafety DAG系统                     │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐  │
│  │   节点A     │────│   节点B     │────│   节点C     │  │
│  │             │    │             │    │             │  │
│  │ Function    │    │ Function    │    │ Function    │  │
│  │ Router      │    │ Router      │    │ Router      │  │
│  └─────────────┘    └─────────────┘    └─────────────┘  │
├─────────────────────────────────────────────────────────┤
│                      并行执行支持                        │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐  │
│  │  Parallel   │────│  Parallel   │────│  Parallel   │  │
│  │  Function   │    │  Function   │    │  Function   │  │
│  │             │    │             │    │             │  │
│  │ keyword     │    │ fastText    │    │ BERT        │  │
│  │ RAG         │    │ KB Search   │    │ Multi-turn  │  │
│  └─────────────┘    └─────────────┘    └─────────────┘  │
├─────────────────────────────────────────────────────────┤
│                    上下文管理                            │
│  ┌─────────────────────────────────────────────────────┐ │
│  │              SessionContext                         │ │
│  │  - 业务配置  - 历史记录  - 检测内容  - 中间结果       │ │
│  └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 节点配置结构

```yaml
- nodeId: node_id                    # 节点唯一标识
  functionConf:                       # Function配置
    type: function_type               # Function类型
    name: function_name               # Function名称
    timeoutMilliseconds: 200          # 超时时间
    conf: {}                          # 具体配置参数
  routerConf:                         # Router配置
    type: router_type                 # Router类型
    name: router_name                 # Router名称
    conf: {}                          # Router配置参数
  ignoreError: true                   # 是否忽略执行错误
```

---

## Function组件详解

### Function类型总览

| 类型 | 说明 | 适用场景 | 实现类 |
|------|------|----------|--------|
| `dummy` | 空操作，通常用于起始节点 | 策略起始点 | DummyFunction |
| `single_label_pred` | 单标签分类 | 内容分类、风险检测 | BertFunction, FastTextFunction |
| `multi_label_pred` | 多标签分类 | 多维度风险检测 | BertFunction |
| `keyword` | 敏感词检测 | 违规词、黑名单检测 | KeywordFunction |
| `kb_search` | 知识库搜索 | 事实核查、信息检索 | KnowledgeSearchFunction |
| `rag_answer` | 基于RAG的代答 | 红线问题自动回答 | RagAnswerFunction |
| `multi_turn_detect` | 多轮对话检测 | 对话连贯性、上下文风险 | MultiTurnDetectFunction |
| `parallel` | 并行执行多个Function | 提升检测效率 | ParallelFunction |
| `async` | 异步执行 | 耗时较长的检测任务 | AsyncFunction |

### 1. DummyFunction - 空操作

**用途**：作为策略的起始节点，不执行任何检测逻辑。

```yaml
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
      script: |
        def from_robot = ctx.curReq[0].fromRobot()
        return from_robot ? 'robot_branch' : 'user_branch'
```

### 2. KeywordFunction - 敏感词检测

**用途**：检测文本中的敏感词汇，支持黑白名单、正则表达式等。

```yaml
- nodeId: keyword_check
  functionConf:
    type: keyword
    name: keyword_check
    timeoutMilliseconds: 100
    conf:
      ignoreRiskCode:               # 忽略的风险码
        - 2001
  routerConf:
    type: groovy
    name: keyword_router
    conf:
      script: |
        def result = ctx.curResult
        if (result && result.hasRisk()) {
          return null  # 有风险直接结束
        } else {
          return 'next_check'  # 继续下一个检测
        }
```

### 3. ParallelFunction - 并行执行

**用途**：同时执行多个检测能力，提升整体检测效率。

```yaml
- nodeId: parallel_check
  functionConf:
    type: parallel
    name: parallel_user_check
    timeoutMilliseconds: 300
    conf:
      functionConfs:               # 并行执行的Function列表
        - ref: keyword              # 引用已注册的keyword能力
        - ref: fast20250710         # 引用已注册的fast能力
          conf:
            ignoreRiskCode:
              - 2001
              - 2002
        - ref: bert_20250916        # 引用已注册的bert能力
      reduceType: groovy_script     # 结果归约策略
      reduceScript: |               # 归约脚本
        import com.jd.security.llmsec.core.check.MixedCheck

        def results = ctx.curResult.resultMap
        def keyword = results.keyword
        def fast = results.fast20250710
        def bert = results.bert_20250916

        // 优先级：敏感词 > BERT > FastText
        if (keyword?.hasRisk()) {
          return keyword
        } else if (bert?.hasRisk()) {
          return bert
        } else if (fast?.hasRisk()) {
          return fast
        } else {
          return MixedCheck.noRisk()
        }
  routerConf:
    type: groovy
    name: parallel_router
    conf:
      script: |
        def result = ctx.curResult
        return result?.hasRisk() ? null : 'rag_answer'
```

### 4. RagAnswerFunction - RAG代答

**用途**：基于检索增强生成（RAG）技术，为红线问题提供标准答案。

```yaml
- nodeId: rag_answer
  functionConf:
    type: rag_answer
    name: rag_answer
    timeoutMilliseconds: 500
    conf:
      threshold: 0.8               # 相似度阈值
      topK: 3                      # 返回答案数量
  routerConf:
    type: stupid_end               # 直接结束
    name: stupid_end
```

### Function实现机制

#### 核心接口

```java
public interface Function {
    FunctionConfig conf();                    // 配置信息
    RiskCheckType type();                     // Function类型
    String name();                            // Function名称
    RiskCheckResult run(SessionContext context) throws ExceptionWithCode;
}
```

#### 执行流程

```java
public class AbstractFunction implements Function {
    @Override
    public RiskCheckResult run(SessionContext context) throws ExceptionWithCode {
        long startTime = System.currentTimeMillis();
        try {
            // 1. 前置处理
            if (!preProcess(context)) {
                return handlePreProcessFailure(context);
            }

            // 2. 核心逻辑执行
            RiskCheckResult result = doRun(context);

            // 3. 后置处理
            result = postProcess(context, result);

            // 4. 记录执行时间
            result.setCost(System.currentTimeMillis() - startTime);

            return result;
        } catch (Exception e) {
            return handleException(context, e);
        }
    }

    protected abstract RiskCheckResult doRun(SessionContext context) throws ExceptionWithCode;
}
```

---

## Router组件详解

### Router类型总览

| 类型 | 说明 | 配置参数 | 适用场景 |
|------|------|----------|----------|
| `stupid_end` | 直接结束 | 无 | 简单结束场景 |
| `groovy` | 基于Groovy脚本决策 | `script` | 复杂条件判断 |
| `keyword` | 基于敏感词匹配决策 |无 | 基于关键词路由 |
| `user_end` | 用户相关结束 | 无 | 用户消息结束 |
| `robot_end` | 机器人相关结束 | 无 | 机器人消息结束 |

### 1. Groovy Router - 脚本路由

**最灵活的路由方式**，支持复杂的条件判断和业务逻辑。

```yaml
routerConf:
  type: groovy
  name: groovy_router
  conf:
    script: |
      import com.jd.security.llmsec.core.check.RiskCheckType
      import com.jd.security.llmsec.core.check.MixedCheck

      // 获取当前检测结果
      def result = ctx.curResult
      def retMap = ctx.curResult?.resultMap

      // 获取历史检测结果
      def keywordResults = ctx.middleResults[RiskCheckType.keyword]
      def bertResults = ctx.middleResults[RiskCheckType.single_label_pred]

      // 复杂业务逻辑判断
      if (result?.hasRisk() && result.riskCode == 1001) {
          // 严重风险，直接结束
          return null
      } else if (retMap?.keyword?.bwgLabel == 1) {
          // 敏感词命中红线，进入代答流程
          return 'rag_answer'
      } else if (retMap?.fast20250710?.probability > 0.8) {
          // FastText高置信度风险，进入BERT检测
          return 'bert_check'
      } else {
          // 其他情况继续检测
          return 'next_check'
      }
```

#### Groovy脚本内置变量

| 变量 | 类型 | 说明 |
|------|------|------|
| `ctx` | SessionContext | 会话上下文对象 |
| `retMap` | Map<String, RiskCheckResult> | 当前节点的结果映射 |
| `curResult` | RiskCheckResult | 当前节点的检测结果 |
| `middleResults` | Map<RiskCheckType, List<RiskCheckResult>> | 中间结果集合 |

### 2. Keyword Router - 关键词路由

**基于关键词匹配进行路由决策**。

```yaml
routerConf:
  type: keyword
  name: keyword_router
```

### 3. Stupid End Router - 直接结束

**最简单的路由方式**，无条件结束策略执行。

```yaml
routerConf:
  type: stupid_end
  name: stupid_end
```

---

## SessionContext会话上下文

### SessionContext结构

```java
@Data
public class SessionContext {
    // 基础配置
    private BusinessConf businessConf;                   // 业务配置信息
    private String businessName;                         // 业务名称
    private String sessionId;                            // 会话ID

    // 请求信息
    private List<DefenseApiRequest> curReq;             // 当前请求列表
    private String checkContent;                        // 待检测内容
    private Map<String, Object> requestMetadata;        // 请求元数据

    // 执行状态
    private Node currentNode;                           // 当前执行节点
    private RiskCheckResult curResult;                  // 当前节点结果
    private Map<RiskCheckType, List<RiskCheckResult>> middleResults; // 中间结果

    // 历史信息
    private List<History> histories;                    // 历史对话记录

    // 执行信息
    private long executeStartTime;                      // 开始执行时间
    private long executeEndTime;                        // 结束执行时间
    private String endReason;                           // 结束原因
    private List<String> executedNodes;                 // 已执行节点列表
    private Map<String, Long> nodeCosts;                // 各节点耗时统计

}
```

### 关键方法

```java
public class SessionContext {
    /**
     * 判断是否来自机器人
     */
    public boolean fromRobot();
}
```

---

## 策略配置实战

### 实战案例1：基础敏感词检测策略

```yaml
businessName: keyword_only
group: default
desc: '仅敏感词'
rootId: start
version: 1
confArray:
  - nodeId: start
    functionConf:
      ref: keyword
    routerConf:
      type: stupid_end
      name: stupid_end
    ignoreError: true
```

### 实战案例2：多层纵深防御策略

```yaml
businessName: test
group: default
desc: 带红线代答的纵深防御策略
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
              return 'from_robot1'
          } else {
              return 'from_user1'
          }
    ignoreError: true
  - nodeId: from_user1
    functionConf:
      type: parallel
      name: parallel_from_user1
      timeoutMilliseconds: 200
      conf:
        functionConfs:
          - ref: keyword
          - ref: fast20250710
            conf:
              ignoreRiskCode:
                - 2001
    routerConf:
      type: groovy
      name: groovy
      conf:
        script: |-
          import com.jd.security.llmsec.core.check.MixedCheck
          
          def retMap=ctx.curResult.resultMap
          def next="from_user2_bert"
          
          def kw = retMap.keyword
          def fast = retMap.fast20250710
          
          if (kw == null && fast == null) { // 黑白名单和fasttext都为空，返回正常
              ctx.curResult=MixedCheck.noRisk()
              return null
          } else if (kw != null && (kw.bwgLabel == 1 || kw.bwgLabel == 2 )){ // 命中黑/白，直接返回
              ctx.curResult = kw
              return null
          } else if (kw != null && kw.bwgLabel == 3)  { // 命中灰，走后面
              return next
          } else if (fast != null && !fast.hasRisk()) { // fasttext无恶意，直接返回
              ctx.curResult = fast
              return null
          } else if (fast == null) { // fasttext失败，直接返回，避免因fasttext异常导致的雪崩
              ctx.curResult=MixedCheck.noRisk()
              return null
          } else { // 走bert
              return next
          }
    ignoreError: true
  - nodeId: from_user2_bert
    functionConf:
      type: parallel
      name: parallel_from_user2
      timeoutMilliseconds: 200
      conf:
        functionConfs:
          - ref: rag_answer
            conf:
              threshold: 0.8
          - ref: bert_20250916
            conf:
              ignoreRiskCode:
                - 2001
    routerConf:
      type: groovy
      name: groovy
      conf:
        script: |-
          import com.jd.security.llmsec.core.check.RiskCheckType
          import com.jd.security.llmsec.core.check.MixedCheck
          
          def rag_answers = ctx.middleResults[RiskCheckType.rag_answer]
          if (rag_answers != null && rag_answers[0].riskCode > 0) {
            rag_answer = rag_answers[0]
            ctx.curResult = rag_answer
            return null
          }
          
          def bert = null
          def fast=null
          def single_label_results = ctx.middleResults[RiskCheckType.single_label_pred]
          
          //取fast结果
          if (single_label_results != null) {
              for (item in single_label_results) {
                  if (item.srcName.contains("fast20250710")) {
                      fast = item
                  }
                  if (item.srcName.contains("bert_20250916")) {
                      bert = item
                  }
              }
          }
          
          if (bert != null && bert.hasRisk() && fast != null && fast.hasRisk() \
           && bert.riskCode==fast.riskCode && bert.probability > 0.5 && fast.probability > 0.5) {
              ctx.curResult = bert
          } else if (fast != null && fast.hasRisk() && bert == null && fast.probability > 0.99) {
              ctx.curResult = fast
          } else {
              ctx.curResult = MixedCheck.noRisk()
          }
          return null
    ignoreError: true
  - nodeId: from_robot1
    functionConf:
      ref: keyword
    routerConf:
      type: keyword
      name: keyword_end
    ignoreError: true
```

### 实战案例3：提示词注入检测策略

```yaml
businessName: test
group: default
desc: '提示词注入检测的纵深防御策略'
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
          def thresholds_common = [0:0.9, 1001:0.9, 1002:0.9, 1003:0.9, 1004:0.9, 1005:0.9, 1006:0.9, 1007:0.9, 2001:0.9, 3002:0.9, 4002:0.9, 5001:0.9, 5002:0.9]
          def thresholds_inject = [0:0.9, 201:0.9]
          
          if (bert_common.hasRisk() && bert_common.probability >= thresholds_common[bert_common.riskCode]) {
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
```


## 问题与解决方案

### Q1: 如何处理Function执行超时？

**问题**：Function执行时间过长影响整体性能。

**解决方案**：
1. **合理设置超时时间**：function超时时间根据Function类型和历史耗时设置，同时设置总的超时时间
2. **异步执行**：对于耗时较长的Function支持异步执行
3. **降级策略**：超时时使用降级逻辑


### Q2: 如何优化Groovy脚本性能？

**问题**：复杂的Groovy脚本影响策略执行性能。

**解决方案**：
1. **脚本预编译**：在策略加载时预编译Groovy脚本
2. **缓存脚本引擎**：复用ScriptEngine实例
3. **简化逻辑**：将复杂逻辑拆分到多个节点


### Q3: 如何处理策略配置热更新？

**问题**：如何在不重启服务的情况下更新策略配置？

**解决方案**：
1. **配置监听**：监听配置变更事件
2. **平滑切换**：使用版本号实现配置平滑切换
3. **状态验证**：验证新配置的正确性
