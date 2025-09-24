set -eox

: "${SAFETY_MODEL_DIR:?环境变量 SAFETY_MODEL_DIR 未设置}"
: "${REDIS_PASSWORD:?环境变量 REDIS_PASSWORD 未设置}"
: "${MYSQL_ROOT_PASSWORD:?环境变量 MYSQL_ROOT_PASSWORD 未设置}"
: "${MYSQL_PASSWORD:?环境变量 MYSQL_PASSWORD 未设置}"

deploy_dir=`pwd`
export NETWORK_NAME=joy_safety
if docker network inspect "$NETWORK_NAME" >/dev/null 2>&1; then
    echo $NETWORK_NAME"已存在"
else
    echo $NETWORK_NAME"不存在"
    echo '创建docker网络开始'
    docker network create $NETWORK_NAME
    echo '创建docker网络结束'
fi

echo '启动mysql容器开始'
cd $deploy_dir/thirdparty/mysql \
  && mkdir -p ~/mysql-data \
  && mkdir -p ~/mysql-conf \
  && cp my.cnf ~/mysql-conf/my.cnf \
  && docker run -d \
       --name safety-mysql \
       --network=$NETWORK_NAME \
       -e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD \
       -e MYSQL_DATABASE=safety \
       -e MYSQL_USER=test \
       -e MYSQL_PASSWORD=$MYSQL_PASSWORD \
       -v ~/mysql-data:/var/lib/mysql \
       -v ~/mysql-conf/my.cnf:/etc/my.cnf \
       -p 3306:3306 \
       container-registry.oracle.com/mysql/community-server:8.4
echo '启动mysql容器结束'

echo '启动redis容器开始'
cd $deploy_dir/thirdparty/redis \
  && docker run -d \
     --name safety-redis \
     --network=$NETWORK_NAME \
     -p 6379:6379 redis:8.2.1 \
  && docker exec -it safety-redis redis-cli CONFIG SET requirepass $REDIS_PASSWORD
echo '启动redis容器结束'

echo '启动vearch容器开始'
cd $deploy_dir/thirdparty/vearch \
  && docker run  \
    -d --name safety-vearch \
    -p 8817:8817 -p 9001:9001 \
    --network=$NETWORK_NAME \
    -v `pwd`/config.toml:/vearch/config.toml \
    vearch/vearch:3.5.6 all
echo '启动vearch容器结束'

echo '等待mysql及redis启动完成'
sleep 30

cd $deploy_dir
docker exec -i safety-mysql mysql -h 127.0.0.1 -P 3306 -utest -p$MYSQL_PASSWORD safety < ../safety/safety-admin/docs/tables.sql
docker exec -i safety-mysql mysql -h 127.0.0.1 -P 3306 -utest -p$MYSQL_PASSWORD safety < ../safety/safety-admin/docs/sensitive_words.sql
docker exec -i safety-mysql mysql -h 127.0.0.1 -P 3306 -utest -p$MYSQL_PASSWORD safety < ../safety/safety-admin/docs/red_line_knowledge.sql

echo '启动admin容器开始'
cd $deploy_dir/../safety/safety-admin/build \
  && docker run  \
       -d --name safety-admin \
       -p 8006:8006 \
       --network=$NETWORK_NAME \
       -v `pwd`/..:/work/conf \
       -v `pwd`:/work/logs \
       -e MYSQL_PASSWORD=$MYSQL_PASSWORD \
       -e ADMIN_APP_CONF_FILE=/work/conf/docs/application_example.properties \
       -e ADMIN_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
       safety-admin:0.0.1
echo '启动admin容器结束'

echo '等待api启动完成'
sleep 30

echo '启动api容器开始'
cd $deploy_dir/../safety/safety-api/build \
   && docker run  \
        -d --name safety-api \
        -p 8007:8007 \
        --network=$NETWORK_NAME \
        -v `pwd`/..:/work/conf \
        -v `pwd`:/work/logs \
        -e REDIS_PASSWORD=$REDIS_PASSWORD \
        -e API_APP_CONF_FILE=/work/conf/docs/application_example.properties \
        -e API_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
        safety-api:0.0.1
echo '启动api容器结束'

echo '启动keywords容器开始'
cd $deploy_dir/../safety/safety-skills/safety-keywords/build \
  && docker run  \
    -d --name safety-keywords \
    -p 8005:8005 \
    --network=$NETWORK_NAME \
    -v `pwd`/..:/work/conf \
    -v `pwd`:/work/logs \
    -e MYSQL_PASSWORD=$MYSQL_PASSWORD \
    -e KEYWORD_APP_CONF_FILE=/work/conf/docs/application_example.properties \
    -e KEYWORD_LOG_CONF_FILE=/work/conf/docs/log4j2-example.xml \
    safety-keywords:0.0.1
echo '启动keywords容器结束'

echo '启动bert容器开始'
cd $deploy_dir/../safety/safety-skills/safety-bert/build \
  && docker run  \
       -d --name safety-bert \
       -p 8003:8003 \
       --network=$NETWORK_NAME \
       -v ${SAFETY_MODEL_DIR}:/work/models \
       -v `pwd`/..:/work/conf \
       -e BERT_CONFIG_FILE=/work/conf/docs/conf_example.json \
       safety-bert:0.0.1
echo '启动bert容器结束'

echo '启动fasttext容器开始'
cd $deploy_dir/../safety/safety-skills/safety-fasttext/build \
  && docker run  \
       -d --name safety-fasttext \
       -p 8002:8002 \
       --network=$NETWORK_NAME \
       -v ${SAFETY_MODEL_DIR}:/work/models \
       -v `pwd`/..:/work/conf \
       -e FASTTEXT_GUNICORN_CONF=/work/conf/gunicorn.conf.py \
       -e FASTTEXT_CONFIG=/work/conf/docs/conf_example1.json \
       safety-fasttext:0.0.1
echo '启动fasttext容器结束'

echo '启动knowledge容器开始'
cd $deploy_dir/../safety/safety-skills/safety-knowledge/build \
  && docker run  \
       -d --name safety-knowledge \
       -p 8004:8004 \
       --network=$NETWORK_NAME \
       -v ${SAFETY_MODEL_DIR}:/work/models \
       -v `pwd`/..:/work/conf \
       -e KG_CONFIG_FILE=/work/conf/docs/config_example.py \
       safety-knowledge:0.0.1
echo '启动knowledge容器结束'

cd $deploy_dir

