#docker build --progress=plain -t safety-admin:0.0.1 ..
DOCKER_BUILDKIT=1 docker build \
--secret id=mvnsettings,src="${MAVEN_SETTING_FILE:-/home/sunyaofei3/maven/settings.xml}" \
--progress=plain -t safety-admin:0.0.1 ..