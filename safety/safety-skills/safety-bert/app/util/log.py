import logging
import logging.config
import sys
import concurrent_log_handler

from transformers import logging as transformers_logging

prod_format = '%(asctime)s | %(name)s:%(lineno)d | %(processName)s(%(process)d) | %(levelname)s | %(message)s'
local_format = '%(asctime)s | "%(pathname)s:%(lineno)d" | %(processName)s(%(process)d) | %(levelname)s | %(message)s'

def init(debug: bool = False):
    do_reset()
    app_handler = concurrent_log_handler.ConcurrentRotatingFileHandler(
        "app.log",
        backupCount=3,
        maxBytes=1024*1024*100
    )

    # 禁用 transformers 的日志
    transformers_logging.disable_default_handler()
    transformers_logging.set_verbosity_error()

    if not debug:
        logging.basicConfig(level=logging.INFO, format=prod_format, handlers=[app_handler])
    else:
        std_handler = logging.StreamHandler(sys.stdout)
        logging.basicConfig(level=logging.DEBUG, format=local_format, handlers=[app_handler, std_handler])
    logging.info("Logging configured")


def do_reset():
    # 获取 root logger
    root_logger = logging.getLogger()
    # 移除所有 handler
    for handler in root_logger.handlers[:]:
        root_logger.removeHandler(handler)

