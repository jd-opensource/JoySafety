FROM docker.m.daocloud.io/library/python:3.10.18-bookworm

# 安装编译依赖
RUN apt-get update && apt-get install -y build-essential cmake && rm -rf /var/lib/apt/lists/*

WORKDIR /work
COPY ./app /work/app
COPY main.py gunicorn.conf.py requirements.txt run.sh /work/
RUN chmod +x /work/run.sh
RUN pip install -r /work/requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple && pip cache purge

ENTRYPOINT ["/work/run.sh"]
CMD gunicorn -c "${FASTTEXT_GUNICORN_CONF:-./gunicorn.conf.py}"  main:app

