import json
import asyncio
import json
import multiprocessing
import os
import time

import tornado
import tornado.ioloop
import tornado.web
from tornado.httpserver import HTTPServer
from tornado.netutil import bind_sockets

import app.conf.conf as conf
from app.model.bert_infer import init_all
from app.util import log as log_util


# os.environ["TOKENIZERS_PARALLELISM"] = "false"

class HealthCheck(tornado.web.RequestHandler):
    def get(self):
        print("hello world")
        self.write("Hello, world")

def make_app():
    from app.web.input_bert_api import BertHandler
    handlers = [
        (r"/v1/bert/(?P<model_name>[0-9\-a-zA-Z_]{3,})", BertHandler),
        (r"/healthcheck", HealthCheck),
    ]
    return tornado.web.Application(handlers)

def main():
    # 设置多进程启动方式为 'spawn'，解决cuda环境下的问题
    multiprocessing.set_start_method('spawn', force=True)
    if "BERT_CONFIG_FILE" not in os.environ:
        raise Exception("BERT_CONFIG_FILE environment variable is not set")
    conf_file = os.environ.get("BERT_CONFIG_FILE")
    print(f"conf_file={conf_file}")
    conf_obj: conf.Conf = conf.load(conf_file)
    print(f"confs={json.dumps(conf_obj.model_dump(), ensure_ascii=False, indent=1)}")

    if not conf_obj.bert:
        raise Exception("bert configuration is not set")

    process_num = conf_obj.process_num
    process_num = process_num if process_num > 0 else 1
    print(f'process_num={process_num}')
    sockets = bind_sockets(8003)
    if process_num == 1:
        print("单进程启动")
        spawn_main(1, sockets, conf_obj)
    else:
        print("启动多进程")
        # 使用 'spawn' 模式启动子进程
        processes = []
        for i in range(process_num):
            process = multiprocessing.Process(target=spawn_main, args=(i, sockets, conf_obj,))
            process.start()
            processes.append(process)

        # 等待子进程退出
        for process in processes:
            process.join()


def spawn_main(i: int, sockets, conf_obj: conf.Conf):
    start = time.time()
    print(f"{i}开始初始化")
    log_util.init(conf_obj.debug)
    init_all(conf_obj.bert)
    print(f"{i}初始化完成, conf={json.dumps(conf_obj.model_dump(), ensure_ascii=False)}, cost={time.time() - start}s")

    async def run_server():
        server = HTTPServer(make_app())
        server.add_sockets(sockets)
        # 等待事件，不退出
        await asyncio.Event().wait()

    asyncio.run(run_server())


if __name__ == "__main__":
    main()
