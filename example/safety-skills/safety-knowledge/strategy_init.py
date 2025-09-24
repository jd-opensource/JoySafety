import json
import yaml
import requests

ADMIN_HOST = "http://safety-admin:8006"


def post(url, body, headers={'Content-Type': 'application/json'}):
    is_json = headers['Content-Type'] == 'application/json'
    data = body if not is_json else json.dumps(body)
    response = requests.post(url, data=data, headers=headers)
    print(f"url={url}, req={body}, resp={response.text}")
    return json.loads(response.text) if is_json else response.text


def new_strategy(body: str):
    url = ADMIN_HOST + "/config/defense/manage/dag/dagUpdateYaml"
    return post(url, body, headers={'Content-Type': 'text/plain'})


def biz_online(id: int):
    url = ADMIN_HOST + "/config/defense/manage/dag/online"
    return post(url, {"id": id})


if __name__ == '__main__':
    with open('strategy_example.yml') as f:
        strategy = f.read()
        resp = new_strategy(strategy)
        resp_obj = yaml.safe_load(resp)
        biz_online(resp_obj['id'])
