for app in safety-admin safety-bert safety-fasttext safety-knowledge safety-api safety-keywords safety-vearch safety-mysql safety-redis;
  do docker stop $app
    docker rm $app;
    echo '停止并删除'$app'完成';
  done