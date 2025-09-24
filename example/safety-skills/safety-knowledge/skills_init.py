skills = [
    {
        "name": "fast20250710",
        "group": "default",
        "desc": "fast20250710",
        "type": "single_label_pred",
        "timeoutMilliseconds": 1000,
        "status": "online",
        "confObj": {
            "extra": {
                "url": "http://safety-fasttext:8002/fasttext/fast20250710"
            },
            "name": "fast20250710",
            "modelType": "fasttext",
            "ignoreRiskCode": []
        }
    },
    {
        "name": "keyword",
        "group": "default",
        "desc": "敏感词识别",
        "type": "keyword",
        "timeoutMilliseconds": 1000,
        "status": "online",
        "confObj": {
            "name": "敏感词服务",
            "url": "http://safety-keywords:8005/keyword/query"
        }
    },
    {
        "name": "bert_prompt_injection_20250828",
        "group": "default",
        "desc": "bert_prompt_injection_20250828",
        "type": "single_label_pred",
        "timeoutMilliseconds": 2000,
        "status": "online",
        "confObj": {
            "extra": {
                "url": "http://safety-bert:8003/v1/bert/bert_prompt_injection_20250828"
            },
            "name": "bert_prompt_injection_20250828",
            "modelType": "bert",
            "ignoreRiskCode": []
        }
    },
    {
        "name": "bert_20250916",
        "group": "default",
        "desc": "bert_20250916",
        "type": "single_label_pred",
        "timeoutMilliseconds": 2000,
        "status": "online",
        "confObj": {
            "extra": {
                "url": "http://safety-bert:8003/v1/bert/bert_20250916"
            },
            "name": "bert_20250916",
            "modelType": "bert",
            "ignoreRiskCode": []
        }
    },
    {
        "name": "kb_search",
        "group": "default",
        "desc": "红线知识检索",
        "type": "kb_search",
        "timeoutMilliseconds": 2000,
        "status": "online",
        "confObj": {
            "topK": 5,
            "threshold": 0.8,
            "collection": "red_kg_prod_hnsw_bge512_20250826",
            "url": "http://safety-knowledge:8004/knowledge/search"
        }
    },
    {
        "name": "rag_answer",
        "group": "default",
        "desc": "红线代答",
        "type": "rag_answer",
        "timeoutMilliseconds": 5000,
        "confObj": {
            "topK": 5,
            "threshold": 0.8,
            "collection": "hnsw_bge512_20250826",
            "url": "http://safety-knowledge:8004/knowledge/answer"
        }
    },
    {
        "name": "multi_turn_detect",
        "group": "default",
        "desc": "多轮对话检测",
        "type": "multi_turn_detect",
        "timeoutMilliseconds": 3000,
        "status": "online",
        "confObj": {
            "maxTurns": 20,
            "url": "http://safety-knowledge:8004/dialog"
        }
    }
]

import json
import requests
ADMIN_HOST = "http://safety-admin:8006"


def post(url, body):
    response = requests.post(url, data=json.dumps(body), headers={'Content-Type': 'application/json'})
    print(f"url={url}, req={body}, resp={response.text}")
    return json.loads(response.text)


def new_func(body: dict):
    url = ADMIN_HOST + "/config/defense/manage/function/new"
    return post(url, body)


def func_online(id: int):
    url = ADMIN_HOST + "/config/defense/manage/function/online"
    return post(url, {"id": id})


if __name__ == '__main__':
    for skill in skills:
        resp = new_func(skill)
        func_online(resp['data']['id'])
