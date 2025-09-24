FROM docker.m.daocloud.io/library/python:3.10.18-bookworm

WORKDIR /work
COPY ./app /work/app
COPY main.py run.sh requirements.txt /work/
RUN chmod +x /work/run.sh

RUN pip install -r /work/requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple && pip cache purge

ENTRYPOINT ["/work/run.sh"]
CMD python main.py
