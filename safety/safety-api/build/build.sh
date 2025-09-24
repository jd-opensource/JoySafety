DOCKER_BUILDKIT=1 docker build \
--secret id=mvnsettings,src="${MAVEN_SETTING_FILE:-/home/sunyaofei3/maven/settings.xml}" \
--progress=plain -t safety-api:0.0.1 ..