DOCKER_BUILDKIT=1 docker buildx build \
--platform linux/amd64,linux/arm64 \
--progress=plain -t ccr.ccs.tencentyun.com/joysafety/joysafety:safety-fasttext-0.0.1 \
--push ..