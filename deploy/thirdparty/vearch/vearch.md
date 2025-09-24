# 参考vearch项目
> https://github.com/vearch/vearch/blob/master/docs/DeployByDocker.md
> https://github.com/vearch/vearch/blob/master/config/config.toml
> https://hub.docker.com/r/vearch/vearch/tags
> 
> 

```shell
# 容器后台运行
docker run  \
  -d --name safety-vearch \
  -p 8817:8817 -p 9001:9001 \
  --network=my-custom-net \
  -v `pwd`/config.toml:/vearch/config.toml \
  vearch/vearch:3.5.6 all
```
