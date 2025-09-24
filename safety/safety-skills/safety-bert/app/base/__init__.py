from abc import ABC, abstractmethod
from typing import Dict, List

from pydantic import BaseModel
import tornado
import json
import traceback
import time

import logging
logger = logging.getLogger(__name__)

class Request(BaseModel, ABC):
    request_id: str = ""
    business_id: str = ""
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

class LabelConf(BaseModel):
    riskCode: int
    riskMessage: str
    probability: float = 0.0

class LabelResult(LabelConf):
    detail: List[LabelConf]
    text: str

class BaseHandler(tornado.web.RequestHandler, ABC):
    def post(self, model_name: str = None):
        try:
            start = time.time()
            path = self.request.path
            request_str = self.request.body.decode('utf-8')
            request_body = json.loads(self.request.body.decode('utf-8'))
            headers = {k: v for k, v in self.request.headers.items()}
            request = self.parse_request(request_body, headers)
            response: Response = self.do_post(headers, request, model_name)
            response.cost = time.time() - start
            response_str = json.dumps(response.model_dump(), ensure_ascii=False)
            self.do_log(request_str, response_str if 'embedding' not in path else '...')
            self.write(response_str)
        except Exception as e:
            logging.error(traceback.format_exc())
            response = Response.fail(code=1, message=f"exception: {e}", data=None)
            response_str = json.dumps(response.model_dump(), ensure_ascii=False)
            logging.error(response_str)
            self.write(response_str)

    def do_log(self, request_str, response_str):
        logging.info(f'request={request_str}, response={response_str}')

    @abstractmethod
    def do_post(self, headers: Dict[str,str], request: Request, model_name: str) -> Response:
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
    def do_post(self, headers: Dict[str, str], request: Request, model_name: str) -> Response:
        return Response.success(request)

    def parse_request(self, request_body, headers) -> Request:
        return EchoRequest(**request_body)

