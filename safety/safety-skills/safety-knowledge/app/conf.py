from pydantic import BaseModel

class VearchKnowledgeConf(BaseModel):
    master: str
    router: str
    token: str
    db: str = 'db'
    table_name: str

class OpenAIConf(BaseModel):
    openai_host: str
    openai_key: str
    max_retries: int = 0
    openai_config: dict

class DialogCheckConf(OpenAIConf):
    prompt_principle: str = ''

class KnowledgeConf(BaseModel):
    vearch: VearchKnowledgeConf = None
    min_score: float = 0.75

class AnswerConf(OpenAIConf):
    answer_knowledge_similarity_threshold: float = 0.6
    answer_prompt_template: str

class Conf(BaseModel):
    debug: bool = False
    process_num: int = 1
    knowledge: KnowledgeConf
    answer: AnswerConf
    dialog: DialogCheckConf
