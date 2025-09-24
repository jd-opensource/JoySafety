import json
import requests

ADMIN_HOST = "http://safety-admin:8006"


def post(url, body):
    response = requests.post(url, data=json.dumps(body), headers={'Content-Type': 'application/json'})
    print(f"url={url}, req={body}, resp={response.text}")
    return json.loads(response.text)


def new_biz(body: dict):
    url = ADMIN_HOST + "/config/defense/manage/business/new"
    return post(url, body)


def biz_online(id: int):
    url = ADMIN_HOST + "/config/defense/manage/business/online"
    return post(url, {"id": id})


if __name__ == '__main__':
    resp = new_biz(
        {
            "name": "test",
            "group": "default",
            "desc": "测试一下",
            "type": "toC",
            "secretKey": "123456",
            "qpsLimit": 20,
            "robotCheckConfObj": {
                "checkNum": 1,
                "unit": "slice_single",
                "timeoutMilliseconds": 5000
            },
            "userCheckConfObj": {
                "checkNum": 1,
                "unit": "message_single",
                "timeoutMilliseconds": 5000
            }
        }
    )

    biz_online(resp['data']["id"])
