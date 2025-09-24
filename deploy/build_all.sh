set -ex

deploy_dir=`pwd`

export MAVEN_SETTING_FILE="${deploy_dir}/settings.xml"
: "${MAVEN_SETTING_FILE:?环境变量 MAVEN_SETTING_FILE 未设置}"

echo '构建admin开始'
cd $deploy_dir/../safety/safety-admin/build && sh build.sh
echo '构建admin结束'

echo '构建api开始'
cd $deploy_dir/../safety/safety-api/build && sh build.sh
echo '构建api结束'

echo '构建keywords开始'
cd $deploy_dir/../safety/safety-skills/safety-keywords/build && sh build.sh
echo '构建keywords结束'

echo '构建bert开始'
cd $deploy_dir/../safety/safety-skills/safety-bert/build && sh build.sh
echo '构建bert结束'

echo '构建fasttext开始'
cd $deploy_dir/../safety/safety-skills/safety-fasttext/build && sh build.sh
echo '构建fasttext结束'

echo '构建knowledge开始'
cd $deploy_dir/../safety/safety-skills/safety-knowledge/build && sh build.sh
echo '构建knowledge结束'

cd $deploy_dir