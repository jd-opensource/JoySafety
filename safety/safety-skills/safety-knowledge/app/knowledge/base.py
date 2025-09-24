from typing import List, Dict, Optional

from pydantic import BaseModel, Field

from app.base import Request


class KnowledgeBase(Request):
    cluster: Optional[str] = Field(default=None)
    collection: Optional[str] = Field(default=None)
    brute_search: Optional[bool] = Field(default=True)


class Doc(BaseModel):
    id: Optional[str] = Field(default=None)
    text: str
    source: str
    meta: dict = {}


class KnowledgeUpsertRequest(KnowledgeBase):
    docs: List[Doc] = []


class KnowledgeDeleteRequest(KnowledgeBase):
    ids: List[str] = []
    texts: List[str] = []


class KnowledgeSearchRequest(KnowledgeBase):
    return_embeddings: bool = True
    threshold: float = 0.8
    top_k: int = 5

    filters: Dict[str,List[str]] = None
    text_list: List[str]


class KnowledgeResponseData(BaseModel):
    ids: List[str]
    scores: List[float]
    text_list: List[str]
    embeddings: List[List[float]]
    metadatas: List[Dict[str, object]]

# kgConf: KnowledgeConf = None
#
# def init(conf: KnowledgeConf, is_brute_search: int = 1):
#     global kgConf
#     kgConf = conf
#     vearch = VectorEngine(host=conf.vearch.router,
#                           token=conf.vearch.token,
#                           db_name=conf.vearch.db,
#                           space_name=conf.vearch.table_name)
#     return vearch

# def _add_docs(vearch: VectorEngine, texts, metas):
#     return vearch.add_texts(texts, metas)

# def add_docs(vearch: VearchClusterX, texts, ids, metas):
#     metas = [{'source': id, **meta} for id, meta in zip(ids, metas)]
#     vearch.add_texts(texts, metas)


# def search(vearch: VectorEngine, req: KnowledgeSearchRequest) -> List[KnowledgeResponseData]:
#     """
#     t = vearch.similarity_search_with_score_and_id("你好")
#     print(t)
#
#     (['1234561'], [(Document(page_content='你好', metadata={'source': '11111'}), 1.000000238418579)])
#
#     详见VearchClusterX
#     """
#
#     # todo vearch不支持bulk搜索，暂时只查第一个
#     result: Tuple[List, List[Tuple[Document, float]]] = vearch.similarity_search_with_score_and_id(cleaner.clean(req.text_list[0]),
#                                                                                                    k=req.top_k, filters=req.filters)
#     ids, scores, text_list, embeddings, metadatas = result[0],[],[],[],[]
#     result_ids = []
#     for idx, obj in enumerate(result[1]):
#         knowledge, score = obj[0], obj[1]
#         if score < req.threshold:
#             continue
#         result_ids.append(ids[idx])
#         scores.append(score)
#         content = knowledge.page_content.strip()
#         text_list.append(content)
#         meta: dict = knowledge.metadata
#         if req.return_embeddings:
#             embeddings.append(meta['vector']['feature'])
#         meta.pop('vector')
#
#         if 'safeChat' in meta:
#             meta['safeChat'] = meta['safeChat'].strip()
#         metadatas.append(meta)
#
#     return [KnowledgeResponseData(ids=result_ids, scores=scores, text_list=text_list, embeddings=embeddings, metadatas=metadatas)]
