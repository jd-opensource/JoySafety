import logging

from app.conf import KnowledgeConf
from app.knowledge.base import *
from app.util.vector_engine import VectorEngine

logger = logging.getLogger(__name__)

kg_conf: KnowledgeConf = None
vearch: VectorEngine = None


def init(conf: KnowledgeConf):
    global kg_conf, vearch
    kg_conf = conf
    vearch = VectorEngine(host=conf.vearch.router,
                          token=conf.vearch.token,
                          db_name=conf.vearch.db,
                          space_name=conf.vearch.table_name)


def convert(docs: List[Doc]):
    texts, metas = [], []
    for doc in docs:
        texts.append(doc.text)
        meta = {'source': doc.source, **doc.meta}
        if doc.id:
            meta['_id'] = doc.id
        metas.append(meta)
    return texts, metas


def upsert(req: KnowledgeUpsertRequest):
    texts, metas = convert(req.docs)
    return vearch.upsert(texts, metas, space_name=req.collection)


def delete(req: KnowledgeDeleteRequest):
    return vearch.delete(req.texts, req.ids, space_name=req.collection)


def search(req: KnowledgeSearchRequest) -> List[KnowledgeResponseData]:
    return vearch.search(req.text_list,
                         filter=req.filters,
                         top=req.top_k,
                         threshold=req.threshold,
                         space_name=req.collection,
                         return_emb=req.return_embeddings,
                         )
