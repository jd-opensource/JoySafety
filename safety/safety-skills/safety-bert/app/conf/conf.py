import os,sys
import json
from typing import List

from pydantic import BaseModel, Field

from app.base import LabelConf

class BertConf(BaseModel):
    name: str
    model_path: str
    tokenizer_path: str
    device_type: str
    num_labels: int
    labels: List[LabelConf]

class Conf(BaseModel):
    debug: bool = True
    process_num: int = 20
    bert: List[BertConf] = Field(default=list)


def load(file_name: str) -> Conf:
    with open(file_name, 'r') as f:
        conf_dict = json.load(f)
        return Conf(**conf_dict)

# conf: Conf = None
