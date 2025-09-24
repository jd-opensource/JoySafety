# 指定配置文件启动
docker run --rm \
  -p 8002:8002 \
  -v ${SAFETY_MODEL_DIR}:/work/models \
  -v `pwd`/..:/work/conf \
  -e FASTTEXT_GUNICORN_CONF=/work/conf/gunicorn.conf.py \
  -e FASTTEXT_CONFIG=/work/conf/docs/conf_example1.json \
  safety-fasttext:0.0.1

# 只查看容器环境
docker run -it --rm \
  -p 8002:8002 \
  -v ${SAFETY_MODEL_DIR}:/work/models \
  -v `pwd`/..:/work/conf \
  -e FASTTEXT_GUNICORN_CONF=/work/conf/gunicorn.conf.py \
  -e FASTTEXT_CONFIG=/work/conf/docs/conf_example1.json \
  safety-fasttext:0.0.1 bash

# 后台启动
docker run  \
  -d --name safety-fasttext \
  -p 8002:8002 \
  --network=my-custom-net \
  -v ${SAFETY_MODEL_DIR}:/work/models \
  -v `pwd`/..:/work/conf \
  -e FASTTEXT_GUNICORN_CONF=/work/conf/gunicorn.conf.py \
  -e FASTTEXT_CONFIG=/work/conf/docs/conf_example1.json \
  safety-fasttext:0.0.1

# 查看容器目录
docker run --rm -it safety-fasttext:0.0.1 ls -lh

