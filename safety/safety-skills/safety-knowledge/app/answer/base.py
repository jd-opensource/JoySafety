from app.knowledge.base import *

class AnswerSearchRequest(KnowledgeSearchRequest):
    # 文档数据提供给llm做rag的阈值
    threshold: float = 0.8
    # 认为文档与输入相同的阈值
    same_threshold: float = 0.9


reason_default = '有数据'
reason_no_data = '无数据'
reason_llm_invalid = '模型输出异常字符'
reason_answer_doc_not_same = '模型输出与文档相似度低'
reason_timeout = '执行超时'


class AnswerResponse(BaseModel):
    answer: str = ''
    answer_backup: str = ''
    no_answer_reason: str = reason_default
    answer_ref_similar: float = 0.0
    refDocs: KnowledgeResponseData = None
