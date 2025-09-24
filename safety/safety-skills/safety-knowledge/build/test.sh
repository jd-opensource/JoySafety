# 指定配置启动
docker run --rm \
  -p 8004:8004 \
  -v `pwd`/..:/work/conf \
  -e KG_CONFIG_FILE=/work/conf/docs/config_example.py \
  safety-knowledge:0.0.1

# 查看容器环境
docker run -it --rm \
  -p 8004:8004 \
  -v ${SAFETY_MODEL_DIR}:/work/models \
  -v `pwd`/..:/work/conf \
  -e KG_CONFIG_FILE=/work/conf/docs/config_example.py \
  safety-knowledge:0.0.1 bash

# 后台启动
docker run  \
  -d --name safety-knowledge \
  -p 8004:8004 \
  --network=my-custom-net \
  -v ${SAFETY_MODEL_DIR}:/work/models \
  -v `pwd`/..:/work/conf \
  -e KG_CONFIG_FILE=/work/conf/docs/config_example.py \
  safety-knowledge:0.0.1

# 建表&建库
docker run -it --rm \
  -p 8004:8004 \
  --network=my-custom-net \
  -v ${SAFETY_MODEL_DIR}:/work/models \
  -v `pwd`/..:/work/conf \
  -e KG_CONFIG_FILE=/work/conf/docs/config_example.py \
  safety-knowledge:0.0.1 python /work/conf/docs/vearch_manage.py

# 只看下目录
docker run --rm -it safety-knowledge:0.0.1 ls -lh

