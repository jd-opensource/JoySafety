from app.answer.base import AnswerSearchRequest
from app.knowledge.base import KnowledgeResponseData


def build_prompt(answer_prompt_template: str, request: AnswerSearchRequest, doc: KnowledgeResponseData) -> str:
    query = request.text_list[0]
    qas = []
    for score, question, metadata in zip(doc.scores, doc.text_list, doc.metadatas):
        if score < request.threshold:
            continue
        question = question.strip()
        answer = metadata['safeChat'].strip()
        answer = answer if len(answer) < 500 else answer[:500]
        qas.append(f"""
输入：{question}
回复：{answer}
""")
    # todo 硬截断的问题
    if not qas:
        return None
    qa_join = ('-' * 2).join(qas)
    return answer_prompt_template.format(query=query, reference=qa_join)