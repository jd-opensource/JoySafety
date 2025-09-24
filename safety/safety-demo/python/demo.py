import base64
import hmac
import hashlib
import sys
import time
import json
import requests


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
    timestamp = int(time.time()*1000)
    body["timestamp"] = timestamp
    body["accessTarget"] = "defenseV2"
    if 'plainText' not in body or not body['plainText']:
        body['plainText'] = body['content']
    text = "&".join([k + "=" + str(body[k]) for k in ["accessKey", "accessTarget", "requestId", "timestamp", "plainText"]])
    sig = gen_sign(text, secret_key)
    body['signature'] = sig
    return body


def request(url, accessSecret, body):
    body = do_sign(body, accessSecret)
    response = requests.post(url, data=json.dumps(body), headers={'Content-Type': 'application/json'})
    return response

if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("usage: python demo.py <url> <accessSecret> <json_body_str>")
        exit(1)

    url, accessSecret, body_str = sys.argv[1], sys.argv[2], sys.argv[3]
    body = json.loads(body_str)
    response = request(url, accessSecret, body)
    print("返回：")
    print(response.text)
    print(json.dumps(json.loads(response.text), indent=1, ensure_ascii=False))

'''
python3 demo.py http://safety-api:8007/llmsec/api/defense/v2/test 123456 '{
  "accessKey": "test",
  "businessType": "toC",
  "content": "你好",
  "contentType": "text",
  "messageInfo": {
    "ext": {},
    "fromId": "user123456",
    "fromRole": "user",
    "messageId": 12,
    "sessionId": "sss12345",
    "sliceId": null,
    "toId": "robot1",
    "toRole": "robot"
  },
  "plainText": "",
  "requestId": "9842284a-8949-485f-8c01-317dffb4d6d6",
  "responseMode": "sync",
  "signature": "",
  "timestamp": 1715217074620
}'
'''