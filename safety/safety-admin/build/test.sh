# 指定配置文件启动
docker run --rm \
  -p 8006:8006 \
  -v `pwd`/..:/work/conf \
  -e ADMIN_APP_CONF_FILE=/work/conf/docs/application_example.properties \
  -e ADMIN_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
  safety-admin:0.0.1

# 连接容器中的mysql
docker run --rm \
  -p 8006:8006 \
  --network=my-custom-net \
  -v `pwd`/..:/work/conf \
  -v `pwd`:/work/logs \
  -e ADMIN_APP_CONF_FILE=/work/conf/docs/application_example.properties \
  -e ADMIN_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
  safety-admin:0.0.1

# 容器后台运行
docker run  \
  -d --name safety-admin \
  -p 8006:8006 \
  --network=my-custom-net \
  -v `pwd`/..:/work/conf \
  -v `pwd`:/work/logs \
  -e ADMIN_APP_CONF_FILE=/work/conf/docs/application_example.properties \
  -e ADMIN_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
  safety-admin:0.0.1

# 只查看容器环境
docker run -it --rm \
  -p 8006:8006 \
  -v `pwd`/..:/work/conf \
  -e ADMIN_APP_CONF_FILE=/work/conf/gunicorn.conf.py \
  -e ADMIN_LOG_CONF_FILE=/work/conf/docs/conf_example1.json \
  safety-admin:0.0.1 bash

# 查看容器目录
docker run --rm -it safety-admin:0.0.1 ls -lh

