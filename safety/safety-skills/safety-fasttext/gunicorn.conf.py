import os
import multiprocessing

pidfile = 'gunicorn.pid'

# 监听地址和端口
bind = "0.0.0.0:8002"

# 工作模式: sync / gthread / gevent / uvicorn.workers.UvicornWorker 等
worker_class = "gthread"

# 工作进程数（CPU 核心数 * 2 + 1 通常是个合理值）
# workers = multiprocessing.cpu_count() * 2 + 1
workers = 1

# 每个 worker 的线程数（仅 gthread 模式下有效）
threads = 4

# 每个 worker 的最大并发数 (仅 async worker)
# worker_connections = 1000

# 超时时间 (秒)
timeout = 30

# 请求头大小（默认 8192），有时候需要调大
limit_request_line = 4094
limit_request_fields = 100
limit_request_field_size = 8190

# 日志配置
loglevel = os.environ.get("LOG_LEVEL", "debug")

# 守护进程模式 (不建议 Docker 环境下使用)
# daemon = True

# 优雅重启参数
graceful_timeout = 30
reload = False    # 开发环境可设 True，代码变动时自动重载