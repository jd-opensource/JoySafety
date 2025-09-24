config = {
    # 是否开启debug
    "debug": True,

    # web服务进程数
    "process_num": 1,

    # 多轮对话检测相关配置
    'dialog': {
        # 检测的规则可以按需添加
        'prompt_principle': '''# 审核细则
- 0表示正面，1表示负面
''',
        # openai格式模型的相关配置
        "openai_host": "xxx",
        "openai_key": "xxx",
        "openai_config": {
            "model": "xxx",
            "temperature": 0.1,
            "top_p": 1,
            "max_tokens": 512,
            "timeout": 10,
            #     other more
        }
    },

    # 知识库相关配置
    'knowledge': {
        # vearch引擎配置
        'vearch': {
            "master": "http://safety-vearch:9001",
            "router": "http://safety-vearch:9001",
            "token": "",
            "db": "db0",
            "table_name": "hnsw_bge512_20250826"
        },
    },

    # 基于知识的回复相关配置
    'answer': {
        "openai_host": "xxx",
        "openai_key": "xxx",
        "openai_config": {
            "model": "xxx",
            "temperature": 0.1,
            "top_p": 1,
            "max_tokens": 512,
            "timeout": 10,
            #     other more
        },

        # 最终回复与参考知识的相似度阈值
        "answer_knowledge_similarity_threshold": 0.7,

        # 回复prompt模板
        "answer_prompt_template": """# 目标
你是一个严格遵守中国法律法规、深刻理解并践行社会主义核心价值观的AI应用。你的核心准则是：政治正确、信息准确、保密与合规。请依据参考答案及用户的输入给出最终的回复。

# 参考答案
{reference}

# 约束
- 回复的内容必须与用户的输入的问题相对应；
- 回复时的内容必须包含在**参考答案**中；
- 如果你认为参考答案与用户输入无关，请输出**不知道**；
- 你的回复需要**尽量简洁**！
- 你的回复需要尽量**简单明了**！
- 你的回复需要**尽量直接**！

好了，我们现在正式开始~

输入：{query}
回复：
"""
    }
}
