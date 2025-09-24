# JoySafety 项目 FAQ（结构化版）

---

## 1. 启动没报错，起来不能用
> 初始化，根据电脑性能大概需要 1分钟，耐心等待。

**相关链接：**
- [启动说明（README.md）](../README.md)

---

## 2. 镜像下载不下来
> 由于网络差异，欢迎提交 issue。

**相关链接：**
- [GitHub Issues](https://github.com/jd-opensource/JoySafety/issues)

---

## 3. 模型下载太慢或连接不上 huggingface
> 1. 尝试 modelscope  
> 2. （可补充更多建议）

**相关链接：**
- [模型配置说明（README.md）](../README.md)

---

## 4. 启动了，不知道是否成功
> 1. 首先查看端口 8002-8007 是否全部起来，命令：  
>    `netstat -an | grep 8002`
> 2. 查看容器日志：  
>    `docker compose logs {safety-admin}`  
>    容器名称可通过 `docker ps` 获取
> 3. 某个容器异常可单独重启：  
>    `docker-compose --env-file .env up {容器名称}`
> 4. 全部重启：  
>    `docker-compose --env-file .env down`  
>    `docker-compose --env-file .env up`
> 5. 还是没成功请提交 issue

**相关链接：**
- [容器与端口说明（docker-compose.yaml）](../docker-compose.yaml)
- [启动命令参考（README.md）](../README.md)
- [GitHub Issues](https://github.com/jd-opensource/JoySafety/issues)

---

## 5. 如何测试
> 详见 quickstart readme。

**相关链接：**
- [快速测试说明（quickstart/README.md）](../quickstart/README.md)

---

## 6. safety-example-init 为啥退出啦
> 这个容器为临时镜像，目的是为了快速初始化，不提供服务。

**相关链接：**
- [示例说明（example/README.md）](../example/README.md)
- [容器配置（docker-compose.yaml）](../docker-compose.yaml)
