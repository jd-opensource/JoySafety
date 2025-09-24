```shell
# https://dev.mysql.com/doc/refman/8.4/en/docker-mysql-getting-started.html
# https://dev.mysql.com/doc/refman/8.4/en/docker-mysql-more-topics.html
docker pull container-registry.oracle.com/mysql/community-server:8.4

docker network create my-custom-net

mkdir -p ~/mysql-data
mkdir -p ~/mysql-conf
cp my.cnf ~/mysql-conf/my.cnf

docker run -d \
  --name safety-mysql \
  --network=my-custom-net \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=safety \
  -e MYSQL_USER=test \
  -e MYSQL_PASSWORD=123456 \
  -v ~/mysql-data:/var/lib/mysql \
  -v ~/mysql-conf/my.cnf:/etc/my.cnf \
  -p 3306:3306 \
  container-registry.oracle.com/mysql/community-server:8.4

# docker exec -it mysql bash
# mysql -h 127.0.0.1 -P 3306 -utest -p123456
```