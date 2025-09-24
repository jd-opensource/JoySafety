# 指定配置文件启动
docker run --rm \
  -p 8005:8005 \
  -v `pwd`/..:/work/conf \
  -e KEYWORD_APP_CONF_FILE=/work/conf/docs/application_example.properties \
  -e KEYWORD_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
  safety-keywords:0.0.1

# 连接容器中的mysql
docker run --rm \
  -p 8005:8005 \
  --network=my-custom-net \
  -v `pwd`/..:/work/conf \
  -v `pwd`:/work/logs \
  -e KEYWORD_APP_CONF_FILE=/work/conf/docs/application_example.properties \
  -e KEYWORD_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
  safety-keywords:0.0.1

# 后台启动
docker run  \
  -d --name safety-keywords \
  -p 8005:8005 \
  --network=my-custom-net \
  -v `pwd`/..:/work/conf \
  -v `pwd`:/work/logs \
  -e KEYWORD_APP_CONF_FILE=/work/conf/docs/application_example.properties \
  -e KEYWORD_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
  safety-keywords:0.0.1

# 只查看容器环境
docker run -it --rm \
  -p 8005:8005 \
  -v `pwd`/..:/work/conf \
  -e KEYWORD_APP_CONF_FILE=/work/conf/gunicorn.conf.py \
  -e KEYWORD_LOG_CONF_FILE=/work/conf/docs/conf_example1.json \
  safety-keywords:0.0.1 bash

# 查看容器目录
docker run --rm -it safety-keywords:0.0.1 ls -lh

