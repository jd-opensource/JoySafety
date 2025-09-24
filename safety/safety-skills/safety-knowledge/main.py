import os

import tornado
from tornado.httpserver import HTTPServer
from tornado.netutil import bind_sockets

from app.conf import Conf, KnowledgeConf
from app.util import log as log_util
from app.util.config_helper import load_config
from app.web.answer_api import AnswerHandler
from app.web.dialog_api import DialogCheckHandler
from app.web.knowledge_api import *

logger = logging.getLogger(__name__)

class HealthCheckHandler(tornado.web.RequestHandler):
    def get(self):
        print("hello world")
        self.write("Hello, world")


def make_app():
    return tornado.web.Application([
        (r"/healthcheck", HealthCheckHandler),
        (r"/knowledge/upsert", RedKnowledgeUpsertHandler), #
        (r"/knowledge/delete", RedKnowledgeDeleteHandler), #
        (r"/knowledge/search", RedKnowledgeSearchHandler),
        (r"/knowledge/answer", AnswerHandler),
        (r"/dialog", DialogCheckHandler),
    ])

import app.knowledge.handlers as knowlege
import app.answer.handlers as answer
import app.dialog.handlers as dialog
def init(conf: Conf):
    knowlege.init(conf.knowledge)
    answer.init(conf.answer)
    dialog.init(conf.dialog)
    pass

# https://www.tornadoweb.org/en/stable/guide/running.html
# https://docs.python.org/3/howto/argparse.html#getting-a-little-more-advanced
import asyncio
def main():
    if "KG_CONFIG_FILE" not in os.environ:
        raise Exception("KG_CONFIG_FILE environment variable is not set")
    conf_file = os.environ.get("KG_CONFIG_FILE")
    print(f"conf_file={conf_file}")

    conf_module = load_config(conf_file)
    conf_obj = Conf(**conf_module.config)

    log_util.init(conf_obj.debug)
    init(conf_obj)

    logging.info("初始化完成")

    sockets = bind_sockets(8004)
    if conf_obj.debug:
        print("调试模式不用多进程")
    else:
        print("启动多进程")
        tornado.process.fork_processes(20)

    async def post_fork_main():
        server = HTTPServer(make_app())
        server.add_sockets(sockets)
        await asyncio.Event().wait()
    asyncio.run(post_fork_main())


if __name__ == "__main__":
    main()
