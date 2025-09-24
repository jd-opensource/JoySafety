# 指定配置文件启动
docker run --rm \
  -p 8007:8007 \
  --network=my-custom-net \
  -v `pwd`/..:/work/conf \
  -e API_APP_CONF_FILE=/work/conf/docs/application_example.properties \
  -e API_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
  safety-api:0.0.1

# 只查看容器环境
docker run -it --rm \
  -p 8007:8007 \
  --network=my-custom-net \
  -v `pwd`/..:/work/conf \
  -e API_APP_CONF_FILE=/work/conf/docs/application_example.properties \
  -e API_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
  safety-api:0.0.1 bash

# 容器后台运行
docker run  \
  -d --name safety-api \
  -p 8007:8007 \
  --network=my-custom-net \
  -v `pwd`/..:/work/conf \
  -v `pwd`:/work/logs \
  -e API_APP_CONF_FILE=/work/conf/docs/application_example.properties \
  -e API_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
  safety-api:0.0.1

# 查看容器目录
docker run --rm -it safety-api:0.0.1 ls -lh

