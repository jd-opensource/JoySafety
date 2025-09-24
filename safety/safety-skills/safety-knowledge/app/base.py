from abc import ABC, abstractmethod
from typing import Dict

from pydantic import BaseModel
import tornado
import json
import traceback
import time

import logging
logger = logging.getLogger(__name__)

class Request(BaseModel, ABC):
    request_id: str
    business_id: str
    session_id: str = ""


class Response(BaseModel):
    code: int
    message: str
    cost: float = 0.
    data: object

    @staticmethod
    def success(data: object):
        return Response(code=0, message='success', data=data)

    @staticmethod
    def fail(code: int, message: str, data: object):
        return Response(code=code, message=message, data=data)


class BaseHandler(tornado.web.RequestHandler, ABC):
    def post(self):
        try:
            start = time.time()
            request_str = self.request.body.decode('utf-8')
            request_body = json.loads(request_str)
            headers = {k: v for k, v in self.request.headers.items()}
            request = self.parse_request(request_body, headers)
            logger.info(f'method={self.request.method}, path={self.request.path}, request: {request_str}, headers: {json.dumps(headers, ensure_ascii=False)}')
            response: Response = self.do_post(headers, request)
            response.cost = time.time() - start
            logger.info(f'method={self.request.method}, path={self.request.path}, request: {request_str}, headers: {json.dumps(headers, ensure_ascii=False)}, '
                        f'response: {json.dumps(response.model_dump(), ensure_ascii=False)}')
            self.write(json.dumps(response.model_dump(), ensure_ascii=False))
        except Exception as e:
            logger.error(traceback.format_exc())
            response = Response.fail(code=1, message=f"exception: {e}", data=None)
            logger.error(json.dumps(response.model_dump(), ensure_ascii=False))
            self.write(json.dumps(response.model_dump(), ensure_ascii=False))

    @abstractmethod
    def do_post(self, headers: Dict[str,str], request: Request) -> Response:
        pass

    @abstractmethod
    def parse_request(self, request_body, headers) -> Request:
        pass


class EchoRequest(Request):
    say: str


class EchoHandler(BaseHandler):
    '''
    curl localhost:9999/echo \
-H 'Content-type: application/json' \
-d '{
    "request_id": "123",
    "business_id": "123",
    "session_id": "123",
    "say": "你好呀abc123呀呀工"
}'
    '''
    def do_post(self, headers: Dict[str, str], request: Request) -> Response:
        return Response.success(request)

    def parse_request(self, request_body, headers) -> Request:
        return EchoRequest(**request_body)

