import json
import requests

ADMIN_HOST = "http://safety-admin:8006"
KNOWLEDGE_HOST = "http://safety-knowledge:8004"

import hashlib
def get_md5(text: str):
    # 创建md5对象
    md5 = hashlib.md5()
    # 更新md5对象，这里需要将字符串转换为字节
    md5.update(text.encode('utf-8'))
    # 获取16进制格式的散列值
    return md5.hexdigest()

def post(url, body):
    response = requests.post(url, data=json.dumps(body), headers={'Content-Type': 'application/json'})
    print(f"url={url}, req={body}, resp={response.text}")
    return json.loads(response.text)

def upsert(body: dict):
    url = ADMIN_HOST + "/data/redline_knowledge/upsert"
    return post(url, body)

def get_by_question(body: dict):
    url = ADMIN_HOST + "/data/redline_knowledge/getByWord"
    return post(url, body)

def kg_upsert(body: dict):
    url = KNOWLEDGE_HOST + "/knowledge/upsert"
    return post(url, body)


def kg_delete(body: dict):
    url = KNOWLEDGE_HOST + "/knowledge/delete"
    return post(url, body)

def kg_search(body: dict):
    url = KNOWLEDGE_HOST + "/knowledge/search"
    return post(url, body)

def kg_answer(body: dict):
    url = KNOWLEDGE_HOST + "/knowledge/answer"
    return post(url, body)


def dialog(body: dict):
    url = KNOWLEDGE_HOST + "/dialog"
    return post(url, body)

