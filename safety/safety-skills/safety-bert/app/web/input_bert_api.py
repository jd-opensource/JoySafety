from app.base import BaseHandler, Response, Request
from typing import Dict
import traceback
import logging

from app.base.bert import BertRequest

import app.model.bert_infer as bert_infer
class BertHandler(BaseHandler):
    def do_post(self, headers: Dict[str, str], request: BertRequest, model_name: str) -> Response:
        if not model_name:
            return Response.fail(1, "未指定模型", None)

        if model_name not in bert_infer.bert_model_map:
            return Response.fail(1, "模型不存在", None)

        try:
            try:
                resp = bert_infer.bert_model_map[model_name].infer(request.text_list)
                if resp:
                    return Response.success(resp)
                else:
                    return Response.fail(1, "推理失败", None)
            except Exception as e:
                raise e

        except Exception as e:
            logging.error(traceback.format_exc())
            return Response.fail(1, traceback.format_exc(), None)

    def parse_request(self, request_body, headers) -> Request:
        return BertRequest(**request_body)