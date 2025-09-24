set -ex

deploy_dir=`pwd`
# !!! 如果要构建，需要配置本地setting哦！！！
export MAVEN_SETTING_FILE="${deploy_dir}/settings.xml"

echo '构建admin开始'
cd $deploy_dir/../safety/safety-admin/build && sh buildx.sh
echo '构建admin结束'

echo '构建api开始'
cd $deploy_dir/../safety/safety-api/build && sh buildx.sh
echo '构建api结束'

echo '构建keywords开始'
cd $deploy_dir/../safety/safety-skills/safety-keywords/build && sh buildx.sh
echo '构建keywords结束'

echo '构建bert开始'
cd $deploy_dir/../safety/safety-skills/safety-bert/build && sh buildx.sh
echo '构建bert结束'

echo '构建fasttext开始'
cd $deploy_dir/../safety/safety-skills/safety-fasttext/build && sh buildx.sh
echo '构建fasttext结束'

echo '构建knowledge开始'
cd $deploy_dir/../safety/safety-skills/safety-knowledge/build && sh buildx.sh
echo '构建knowledge结束'

cd $deploy_dir
