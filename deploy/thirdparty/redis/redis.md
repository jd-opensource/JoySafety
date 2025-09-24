# 安装
参考
- https://redis.io/docs/latest/operate/oss_and_stack/install/install-stack/docker/
- https://hub.docker.com/_/redis/tags

```shell
docker run -d \
--name safety-redis \
--network=my-custom-net \
-p 6379:6379 redis:8.2.1

docker exec -it safety-redis redis-cli
CONFIG SET requirepass 123456
```