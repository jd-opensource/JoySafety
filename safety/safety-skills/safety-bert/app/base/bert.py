from typing import List

from app.base import Request, LabelResult


class BertRequest(Request):
    text_list: List[str]

class BertResponseData(LabelResult):
    pass
