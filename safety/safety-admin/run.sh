#!/bin/sh
set -ex

cd /work
echo 'ADMIN_APP_CONF_FILE:'${ADMIN_APP_CONF_FILE}
echo 'ADMIN_LOG_CONF_FILE:'${ADMIN_LOG_CONF_FILE}
echo '$JAVA_OPTS:'${JAVA_OPTS}

exec "$@"