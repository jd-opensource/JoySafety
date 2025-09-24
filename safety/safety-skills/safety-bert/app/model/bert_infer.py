import json
import time
import logging
from typing import List

from app.base import LabelConf
from app.base.bert import BertResponseData

logger = logging.getLogger(__name__)

import torch
from transformers import BertTokenizer, BertForSequenceClassification
import numpy as np
import app.conf.conf as conf

class OnlineModel(object):
    def __init__(self, model_path: str, tokenizer_path: str, device_type: str, model_name: str, labels: List):
        super(OnlineModel, self).__init__()

        device_type = 'cpu' if not device_type else device_type
        self.device = torch.device(device_type)

        self.model_name = model_name
        self.labels = labels

        self.model = BertForSequenceClassification.from_pretrained(model_path, num_labels=len(labels))
        self.model.eval()
        self.model.to(self.device)
        self.tokenizer_path = tokenizer_path
        self.tokenizer = BertTokenizer.from_pretrained(self.tokenizer_path)

    def predict(self, model, text, tokenizer, device):
        inputs = tokenizer(text, truncation=True, padding='max_length', max_length=512, return_tensors='pt',
                           return_attention_mask=True)
        inputs.to(self.device)
        input_ids = inputs['input_ids']
        attention_mask = inputs['attention_mask']
        with torch.no_grad():
            outputs = model(input_ids=input_ids, attention_mask=attention_mask)
            logits = outputs.logits
            probabilities = torch.nn.functional.softmax(logits, dim=1)
            probabilities = probabilities.cpu().numpy()
        return probabilities

    def infer(self, query: List[str]):
        softmaxed = self.predict(self.model, query, self.tokenizer, self.device)
        resp = []
        for i in range(len(softmaxed)):
            item = softmaxed[i]
            max_idx = np.argmax(item)
            max_label = self.labels[max_idx]
            temp = BertResponseData(riskCode=max_label.riskCode, riskMessage=max_label.riskMessage,
                                    probability=item[max_idx],
                                    text=query[i],
                                    detail=[LabelConf(riskCode=self.labels[i].riskCode,
                                                      riskMessage=self.labels[i].riskMessage, probability=item[i]) for i
                                            in range(len(self.labels))]
                                    )
            resp.append(temp)
        return resp

bert_model_map = {}

def init_all(bert_confs: List[conf.BertConf]):
    if not bert_confs:
        return

    for one_config in bert_confs:
        model = init(one_config)
        bert_model_map[one_config.name] = model


def init(bert_conf: conf.BertConf):
    start = time.time()
    logger.info(f'加载模型开始，conf={json.dumps(bert_conf.model_dump(), indent=2, ensure_ascii=False)}')
    _bert = OnlineModel(bert_conf.model_path, bert_conf.tokenizer_path, bert_conf.device_type, bert_conf.name,
                        bert_conf.labels)
    logger.info(
        f'加载模型结束，耗时={time.time() - start}ms, conf={json.dumps(bert_conf.model_dump(), indent=2, ensure_ascii=False)}')
    return _bert
