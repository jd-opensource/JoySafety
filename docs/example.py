import json
import requests

ADMIN_HOST = "http://guard-admin:8006"
skills = [
    {
        "name": "fast20250710",
        "group": "default",
        "desc": "fast20250710",
        "type": "single_label_pred",
        "timeoutMilliseconds": 100,
        "status": "online",
        "confObj": {
            "extra": {
                "url": "http://guard-fasttext:8002/fasttext/fast20250710"
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
        "timeoutMilliseconds": 100,
        "status": "online",
        "confObj": {
            "name": "敏感词服务",
            "url": "http://guard-keywords:8005/keyword/query"
        }
    },
    {
        "name": "bert_prompt_injection_20250623",
        "group": "default",
        "desc": "bert_prompt_injection_20250623",
        "type": "single_label_pred",
        "timeoutMilliseconds": 300,
        "status": "online",
        "confObj": {
            "extra": {
                "url": "http://guard-bert:8003/v1/bert/bert_prompt_injection_20250623"
            },
            "name": "bert_prompt_injection_20250623",
            "modelType": "bert",
            "ignoreRiskCode": []
        }
    },
    {
        "name": "bert_20250819",
        "group": "default",
        "desc": "bert_20250819",
        "type": "single_label_pred",
        "timeoutMilliseconds": 300,
        "status": "online",
        "confObj": {
            "extra": {
                "url": "http://guard-bert:8003/v1/bert/bert_20250819"
            },
            "name": "bert_20250819",
            "modelType": "bert",
            "ignoreRiskCode": []
        }
    },
    {
        "name": "kb_search",
        "group": "default",
        "desc": "红线知识检索",
        "type": "kb_search",
        "timeoutMilliseconds": 300,
        "status": "online",
        "confObj": {
            "topK": 5,
            "threshold": 0.8,
            "collection": "red_kg_prod_hnsw_bge512_20250826",
            "url": "http://guard-knowledge:8004/knowledge/search"
        }
    },
    {
        "name": "rag_answer",
        "group": "default",
        "desc": "红线代答",
        "type": "rag_answer",
        "timeoutMilliseconds": 3000,
        "confObj": {
            "topK": 5,
            "threshold": 0.8,
            "collection": "hnsw_bge512_20250826",
            "url": "http://guard-knowledge:8004/knowledge/answer"
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
            "url": "http://guard-knowledge:8004/dialog"
        }
    }
]


def post(url, body, headers={'Content-Type': 'application/json'}):
    is_json = headers['Content-Type'] == 'application/json'
    data = body if not is_json else json.dumps(body)
    response = requests.post(url, data=data, headers=headers)
    print(f"url={url}, req={body}, resp={response.text}")
    return json.loads(response.text) if is_json else response.text


def new_func(body: dict):
    url = ADMIN_HOST + "/config/defense/manage/function/new"
    return post(url, body)


def func_online(id: int):
    url = ADMIN_HOST + "/config/defense/manage/function/online"
    return post(url, {"id": id})


def new_biz(body: dict):
    url = ADMIN_HOST + "/config/defense/manage/business/new"
    return post(url, body)


def biz_online(id: int):
    url = ADMIN_HOST + "/config/defense/manage/business/online"
    return post(url, {"id": id})

def new_strategy(body: str):
    url = ADMIN_HOST + "/config/defense/manage/dag/dagUpdateYaml"
    return post(url, body, headers={'Content-Type': 'text/plain'})


def strategy_online(id: int):
    url = ADMIN_HOST + "/config/defense/manage/dag/online"
    return post(url, {"id": id})

if __name__ == '__main__':
    # step0: 注册原子能力
    for skill in skills:
        resp = new_func(skill)
        func_online(resp['data']['id'])

    # step1: 新业务接入
    resp = new_biz(
        {
            "name": "test",
            "group": "default",
            "desc": "测试一下",
            "type": "toC",
            "secretKey": "123456",
            "qpsLimit": 20,
            "robotCheckConfObj": {
                "checkNum": 20,
                "unit": "slice_single",
                "timeoutMilliseconds": 2000
            },
            "userCheckConfObj": {
                "checkNum": 1,
                "unit": "message_single",
                "timeoutMilliseconds": 2000
            }
        }
    )

    biz_online(resp['data']["id"])

    # step2: 新的策略接入
    with open('strategy_example.yml') as f:
        strategy = f.read()
        resp = new_strategy(strategy)
        resp_obj = yaml.safe_load(resp)
        strategy_online(resp_obj['id'])



