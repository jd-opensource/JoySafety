import logging

import app.knowledge.handlers as red_kg
from app.base import BaseHandler, Response
from app.knowledge.base import *

logger = logging.getLogger(__name__)


class RedKnowledgeUpsertHandler(BaseHandler):
    def do_post(self, headers: Dict[str, str], request: KnowledgeUpsertRequest) -> Response:
        if not request.docs:
            return Response.fail(1, "docs为空", None)

        inserted_info = red_kg.upsert(request)
        return Response.success(inserted_info)

    def parse_request(self, request_body, headers) -> KnowledgeUpsertRequest:
        return KnowledgeUpsertRequest(**request_body)


class RedKnowledgeDeleteHandler(BaseHandler):
    def do_post(self, headers: Dict[str, str], request: KnowledgeDeleteRequest) -> Response:
        if not request.ids and not request.texts:
            return Response.fail(1, "ids和texts均为空")
        inserted_info = red_kg.delete(request)
        return Response.success(inserted_info)

    def parse_request(self, request_body, headers) -> KnowledgeDeleteRequest:
        return KnowledgeDeleteRequest(**request_body)

class RedKnowledgeSearchHandler(BaseHandler):
    def do_post(self, headers: Dict[str, str], request: KnowledgeSearchRequest) -> Response:
        return Response.success(red_kg.search(request))

    def parse_request(self, request_body, headers) -> Request:
        return KnowledgeSearchRequest(**request_body)