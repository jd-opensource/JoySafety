import json
import requests

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

def kg_upsert(body: dict):
    url = KNOWLEDGE_HOST + "/knowledge/upsert"
    return post(url, body)

config = {
    'user': 'test',
    'password': '123456',
    'host': 'safety-mysql',
    'port': 3306,
    'database': 'safety',
    'connect_timeout': 30,
    'raise_on_warnings': True
}

import mysql.connector
start = 0

def query_data(sql):
    conn = mysql.connector.connect(**config)
    with conn.cursor() as cursor:
        print(f'开始查询数据, sql={sql}')
        cursor.execute(sql)
        records = cursor.fetchall()
        max_id = 0
        ret = []
        for record in records:
            max_id = max(max_id, record[0])
            ret.append(record[1:])
    # 关闭连接
    conn.close()
    return max_id, ret


def do_index(start_id, sql_creator, collection: str = None):
    max_id = start_id
    while True:
        sql = sql_creator(max_id)
        max_id, ret = query_data(sql)
        if not ret:
            break
        else:
            docs = []
            for record in ret:
                business_scene, uniq_id, question, version, class_name, source, type, answer = record

                if len(question) > 500:
                    temp = question[:250] + question[-250:]
                    question = temp

                docs.append(
                    {
                        "id": f"{business_scene}_{uniq_id}_{version}",
                        "text": question,
                        "source": source,
                        "meta": {
                            "safeChat": answer,
                            "className": class_name,
                            "type": 'none' if not type else type
                        }
                    }
                )
            body = {
                "request_id": "123",
                "business_id": "123",
                "session_id": "123",
                "cluster": None,
                "collection": collection,
                "docs": docs
            }
            kg_upsert(body)

if __name__ == '__main__':
    do_index(
        0,
        lambda max_id: f'''select id, business_scene, uniq_id, question, version, class_name, source, type, answer 
    from red_line_knowledge 
    where id >= {max_id + 1} order by id limit 1000''',
        collection='hnsw_bge512_20250826')

    pass
