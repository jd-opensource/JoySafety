import logging
from typing import List, Dict, Union

from vearch.config import Config
from vearch.core.vearch import Vearch
from vearch.filter import Filter, FieldValue, Condition
from vearch.result import SearchResult
from vearch.utils import VectorInfo

from app.knowledge.base import KnowledgeResponseData
from app.util.embedding import bge_zh_small
from app.util.md5 import get_md5


class VectorEngine:
    def __init__(self, host: str, token: str, db_name: str, space_name: str):
        self.host = host
        self.token = token
        self.db_name = db_name
        self.space_name = space_name

        config = Config(host=host, token=token)
        self.vearch = Vearch(config)

    def upsert(self, texts: List[str], metas: List[Dict], space_name: str = None):
        data = []
        for text, meta in zip(texts, metas):
            for f in ['text', 'vector']:
                meta.pop(f, None)

            item = {
                "_id": meta['_id'] if meta.get('_id', '') else get_md5(text),
                "text": text,
                "vector": bge_zh_small.embed_query(text),
                **meta
            }
            data.append(item)
        ret = self.vearch.upsert(self.db_name, space_name if space_name else self.space_name, data)
        if not ret.is_success():
            raise Exception(ret.msg)
        document_ids = ret.get_document_ids()
        logging.info(f'upsert: {document_ids}')
        return document_ids

    def delete(self, texts: List[str], ids: List[str], space_name: str = None):
        if not texts and not ids:
            return

        ids = [get_md5(text) for text in texts] if texts else ids
        resp = self.vearch.delete(self.db_name, space_name if space_name else self.space_name, ids)
        if resp:
            return resp.document_ids
        else:
            return []

    def search(self, texts: List[str],
               filter: Dict[str, List[str]],
               top: int = 5,
               threshold: float = 0.8,
               space_name: str = None,
               return_emb: bool = False) -> List[KnowledgeResponseData]:

        if not texts:
            return []

        vector_infos = [
            VectorInfo("vector",
                       bge_zh_small.embed_query(text),
                       min_score=threshold,
                       max_score=10
                       )
            for text in texts
        ]
        search_filters = None
        if filter:
            conditons_search = []
            for field in filter:
                conditons_search.append(Condition(operator="IN", fv=FieldValue(field, filter[field])))
            search_filters = Filter(operator="AND", conditions=conditons_search)
        raw_result: SearchResult = self.vearch.search(self.db_name,
                                  space_name if space_name else self.space_name,
                                  vector_infos=vector_infos,
                                  filter=search_filters,
                                  vector=return_emb,
                                  limit=top,
                                  )
        result = []
        if not raw_result or not raw_result.documents:
            logging.error('search结果为None')
            return result
        for items in raw_result.documents:
            ids = []
            scores = []
            text_list = []
            embeddings = []
            metadatas = []
            for item in items:
                ids.append(item['_id'])
                scores.append(item['_score'])
                text_list.append(item['text'])
                if return_emb:
                    embeddings.append(item['vector'])
                metadatas.append({k:item[k] for k in item if k not in ['_id', '_score', 'text', 'vector']})

            result.append(KnowledgeResponseData(
                ids=ids, scores=scores, text_list=text_list, embeddings=embeddings, metadatas=metadatas
            ))

        return result
