import logging
from typing import List

from openai import OpenAI

from app.answer.base import AnswerSearchRequest, AnswerResponse, reason_no_data, reason_answer_doc_not_same
from app.answer.prompt import build_prompt
from app.base import Response
from app.conf import AnswerConf
from app.knowledge.base import KnowledgeResponseData
from app.knowledge.handlers import search
from app.util.embedding import bge_zh_small
from app.util.llm_util import invoke_llm

logger = logging.getLogger(__name__)

answer_conf: AnswerConf = None
client: OpenAI = None
openai_config = {}
# https://platform.openai.com/docs/libraries
# https://platform.openai.com/docs/api-reference/chat
def init(conf: AnswerConf):
    global answer_conf, client, openai_config
    answer_conf = conf
    openai_config = conf.openai_config
    client = OpenAI(
        base_url=conf.openai_host,
        api_key=conf.openai_key,
        max_retries=conf.max_retries,
    )

def handle_answer(request: AnswerSearchRequest) -> AnswerResponse:
    docs: List[KnowledgeResponseData] = search(request)

    if not docs:
        return AnswerResponse(no_answer_reason=reason_no_data)
    doc = docs[0]
    if doc.scores and doc.scores[0] >= request.same_threshold:
        doc.embeddings = []
        # 相似程度高时，直接拿来当答案了
        return AnswerResponse(answer=doc.metadatas[0]['safeChat'], answer_ref_similar=doc.scores[0], refDocs=doc)

    prompt_text: str = build_prompt(answer_conf.answer_prompt_template, request, doc)
    if not prompt_text:
        return Response.success(AnswerResponse(no_answer_reason=reason_no_data))
    else:

        messages = [
            {"role": "user", "content": prompt_text}
        ]
        answer = invoke_llm(client, messages, openai_config)

        answer_backup = ''
        reason = ''
        similar = bge_zh_small.cosine(answer, doc.metadatas[0]["safeChat"])
        doc.embeddings = []
        if similar < answer_conf.answer_knowledge_similarity_threshold:
            logger.info(
                f'大模型回复与知识相似度低，request={request}, answer={answer}, doc={doc.metadatas[0]["safeChat"]}')
            answer_backup = answer
            answer = ''
            reason = reason_answer_doc_not_same

        return AnswerResponse(answer=answer, answer_backup=answer_backup, no_answer_reason=reason,
                              answer_ref_similar=similar, refDocs=doc)
