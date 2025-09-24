set -exo

VERSION=0.0.1
# tag
for component in safety-keywords safety-api safety-knowledge safety-fasttext safety-bert safety-admin;
do
  # docker中央仓库
  # docker tag $component:$VERSION  sunyaofei3/joysafety:$component-$VERSION;

  # 腾讯云仓库
  docker tag $component:$VERSION  ccr.ccs.tencentyun.com/joysafety/joysafety:$component-$VERSION;
done

# push 腾讯云
for component in safety-keywords safety-api safety-knowledge safety-fasttext safety-bert safety-admin;
for component in safety-api safety-knowledge safety-fasttext safety-bert safety-admin;
do
  # docker中央仓库
  # docker push sunyaofei3/joysafety:$component-$VERSION;

  # 腾讯云仓库
  docker push ccr.ccs.tencentyun.com/joysafety/joysafety:$component-$VERSION;
done