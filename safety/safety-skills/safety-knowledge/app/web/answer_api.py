import logging

from app.answer.base import *
from app.answer.handlers import handle_answer
from app.base import BaseHandler, Response

logger = logging.getLogger(__name__)

class AnswerHandler(BaseHandler):
    def do_post(self, headers: Dict[str, str], request: AnswerSearchRequest) -> Response:
            return Response.success(handle_answer(request))

    def parse_request(self, request_body, headers) -> Request:
        return AnswerSearchRequest(**request_body)
