# 指定配置启动
docker run --rm \
  -p 8003:8003 \
  -v ${SAFETY_MODEL_DIR}:/work/models \
  -v `pwd`/..:/work/conf \
  -e BERT_CONFIG_FILE=/work/conf/docs/conf_example.json \
  safety-bert:0.0.1

# 查看容器环境
docker run -it --rm \
  -p 8003:8003 \
  -v ${SAFETY_MODEL_DIR}:/work/models \
  -v `pwd`/..:/work/conf \
  -e BERT_CONFIG_FILE=/work/conf/docs/conf_example.json \
  safety-bert:0.0.1 bash

# 后台启动
docker run  \
  -d --name safety-bert \
  -p 8003:8003 \
  --network=my-custom-net \
  -v ${SAFETY_MODEL_DIR}:/work/models \
  -v `pwd`/..:/work/conf \
  -e BERT_CONFIG_FILE=/work/conf/docs/conf_example.json \
  safety-bert:0.0.1

# 只看下目录
docker run --rm -it safety-bert:0.0.1 ls -lh

