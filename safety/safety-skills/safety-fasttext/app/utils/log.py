import logging
import logging.config
import sys
from datetime import date
import concurrent_log_handler

prod_format = '%(asctime)s | %(name)s:%(lineno)d | %(processName)s(%(process)d) | %(levelname)s | %(message)s'
local_format = '%(asctime)s | "%(pathname)s:%(lineno)d" | %(processName)s(%(process)d) | %(levelname)s | %(message)s'

def init(env: str):
    app_handler = concurrent_log_handler.ConcurrentRotatingFileHandler(
        "app.log",
        backupCount=3,
        maxBytes=1024*1024*100
    )

    if 'prod' in env:
        logging.basicConfig(level=logging.INFO, format=prod_format, handlers=[app_handler])
    else:
        std_handler = logging.StreamHandler(sys.stdout)
        logging.basicConfig(level=logging.DEBUG, format=local_format, handlers=[app_handler, std_handler])

