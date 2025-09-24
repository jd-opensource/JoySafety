import logging
from typing import Dict
import json
from app.base import BaseHandler, Response, Request
from app.dialog.handlers import DialogCheckRequest, DialogCheckResponse
from app.dialog import handlers as dialog

logger = logging.getLogger(__name__)

class DialogCheckHandler(BaseHandler):
    def do_post(self, headers: Dict[str, str], request: DialogCheckRequest) -> Response:
        resp: DialogCheckResponse = dialog.handle(request)
        if not resp:
            return Response.success(DialogCheckResponse())
        else:
            logger.info(f"多轮对话，请求={json.dumps(request.model_dump(), ensure_ascii=False)}，返回={json.dumps(resp.dict(), ensure_ascii=False)}")
            return Response.success(resp)

    def parse_request(self, request_body, headers) -> Request:
        return DialogCheckRequest(**request_body)