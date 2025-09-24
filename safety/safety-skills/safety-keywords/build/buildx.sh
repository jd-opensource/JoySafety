DOCKER_BUILDKIT=1 docker buildx build \
--platform linux/amd64,linux/arm64 \
--secret id=mvnsettings,src="${MAVEN_SETTING_FILE:-~/maven/settings.xml}" \
--progress=plain -t ccr.ccs.tencentyun.com/joysafety/joysafety:safety-keywords-0.0.1 \
--push ..