# coding=utf-8

import base64
import hmac
import hashlib
import time
import json
import requests
import random
import string

def hmac_sha1(message, key):
    result = hmac.new(key.encode("utf-8"), message.encode("utf-8"), hashlib.sha1)
    return result.digest()

def gen_sign(text, secret_key):
    try:
        key = base64.b64encode(hmac_sha1(text, secret_key)).decode("utf-8")
        return key
    except Exception as ex:
        print(f"签名异常:{ex}")

def do_sign(body, secret_key):
    timestamp = int(time.time() * 1000)
    body["timestamp"] = timestamp
    body["accessTarget"] = "defenseV2"
    if 'plainText' not in body or not body['plainText']:
        body['plainText'] = body['content']
    text = "&".join(
        [k + "=" + str(body[k]) for k in ["accessKey", "accessTarget", "requestId", "timestamp", "plainText"]])
    sig = gen_sign(text, secret_key)
    body['signature'] = sig
    return body

def do_request(url, accessSecret, body):
    body = do_sign(body, accessSecret)
    response = requests.post(url, data=json.dumps(body), headers={'Content-Type': 'application/json'})
    return response

def random_string(length):
    letters_and_digits = string.ascii_letters + string.digits
    result_str = ''.join(random.choice(letters_and_digits) for _ in range(length))
    return result_str

def request(ak: str, sk: str, body: dict):
    # url = f'http://127.0.0.1:8007/llmsec/api/defense/v2/{ak}'
    url = f'http://safety-api:8007/llmsec/api/defense/v2/{ak}'

    body['requestId'] = random_string(20)
    body['accessKey'] = ak
    body['plainText'] = body['content']

    print(f'输入：{json.dumps(body, ensure_ascii=False)}')
    resp = do_request(url, sk, body)
    resp = json.loads(resp.text)
    print(f'输出：{json.dumps(resp, ensure_ascii=False)}')

    if resp['code'] != 0:
        print(f"请求失败，biz={ak}, req={body}, resp: {resp}")
        return None
    return resp['data'][0]

if __name__ == '__main__':
    import sys
    if len(sys.argv):
        text = sys.argv[1]
        request('test', '123456', body={
            "businessType": "toC",
            "content": text,
            "contentType": "text",
            "messageInfo": {
                "fromId": "user123456",
                "fromRole": 'user',
            },
            "responseMode": "sync",
        })