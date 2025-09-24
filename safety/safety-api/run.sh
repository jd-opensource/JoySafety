#!/bin/sh
set -ex

cd /work
echo 'API_APP_CONF_FILE:'${API_APP_CONF_FILE}
echo 'API_LOG_CONF_FILE:'${API_LOG_CONF_FILE}
echo '$JAVA_OPTS:'${JAVA_OPTS}

exec "$@"