import json
import logging
import time
import traceback

from app.model.fasttext_base import FasttextModel as FAST

logger = logging.getLogger(__name__)

model_map = {}


def init(config_file_name: str):
    with open(config_file_name) as f:
        content = f.read()
        confs = json.loads(content)
        for conf in confs:
            fast = FAST(conf['model_path'], conf['stopwords_path'], k=conf['label_size'],
                        label_mapping=conf['label_mapping'])
            model_map[conf['model_name']] = fast


def classify_by_model_name(req: dict, model_name):
    start_time = time.time()

    try:
        text_list = req.get('text_list', [])
        if not text_list:
            return json.dumps({
                "code": 0,
                "message": "text_list为空",
                "cost": time.time() - start_time,
                "data": []
            }, ensure_ascii=False, indent=4)

        if model_name not in model_map:
            raise Exception('不存在的模型' + model_name)
        infer_resp = model_map[model_name].inference(text_list)
        resp = {
                "code": 0,
                "message": "ok",
                "cost": time.time() - start_time,
                "data": infer_resp
            }
        logging.info(f'req={req}，分类结果：{json.dumps(resp, ensure_ascii=False)}')
        return json.dumps(resp, ensure_ascii=False)
    except Exception as e:
        logger.error(f'调用模型失败，model={model_name}, req={req}, error={traceback.format_exc()}')
        return {
            "code": 1,
            "message": f"调用模型失败,error={str(e)}"
        }