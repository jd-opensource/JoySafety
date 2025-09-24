DOCKER_BUILDKIT=1 docker buildx build \
--platform linux/amd64,linux/arm64 \
--progress=plain -t ccr.ccs.tencentyun.com/joysafety/joysafety:safety-bert-0.0.1 \
--push ..