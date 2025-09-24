from typing import List
import time
import os

import logging
logger = logging.getLogger(__name__)

from onnxruntime import SessionOptions, GraphOptimizationLevel, InferenceSession
from transformers import AutoTokenizer
from numpy import linalg as LA

import numpy as np

class BgeZhSmall:
    # https://huggingface.co/BAAI/bge-small-zh-v1.5
    def __init__(self, model_dir: str, threads: int):
        self.threads = threads
        self.model_dir = model_dir

        options = SessionOptions()  # initialize session options
        options.graph_optimization_level = GraphOptimizationLevel.ORT_ENABLE_ALL
        # 设置线程数
        options.intra_op_num_threads = threads

        self.options = options

        # 这里的路径传上一节保存的onnx模型地址
        session = InferenceSession(
            f'{model_dir}/model.onnx', sess_options=options, providers=["CPUExecutionProvider"]
        )

        # disable session.run() fallback mechanism, it prevents for a reset of the execution provider
        session.disable_fallback()
        self.session = session
        self.tokenizer = AutoTokenizer.from_pretrained(model_dir)

        # warmup
        for i in range(10):
            self.embed_query('预热一下，避免首次调用慢的情况')

    def embed_documents(self, texts: List[str]) -> List[List[float]]:
        ret = []
        for text in texts:
            ret.append(self.embed_query(text))
        return ret

    def embed_query(self, text: str) -> List[float]:
        start = time.time()
        inputs = self.tokenizer(text, padding=True, truncation=True, return_tensors='pt')
        inputs_detach = {k: v.detach().cpu().numpy() for k, v in inputs.items()}
        output = self.session.run(output_names=['logits'], input_feed=inputs_detach)
        embeddings = output[0][:, 0]
        embeddings = embeddings/LA.norm(embeddings)
        logger.debug(f'embedding time: {time.time() - start}')
        return embeddings.tolist()[0]

    def cosine(self, answer: str, doc: str) -> float:
        answer_emb = self.embed_query(answer)
        doc_emb = self.embed_query(doc)
        return np.dot(np.array(answer_emb), np.array(doc_emb))
        pass

bge_zh_small = BgeZhSmall(f'{os.path.dirname(__file__)}{os.path.sep}/bge-small-zh-v1_5', threads=4)

