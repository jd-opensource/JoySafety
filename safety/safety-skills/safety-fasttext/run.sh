#!/bin/sh
set -ex

cd /work
echo 'FASTTEXT_GUNICORN_CONF:'${FASTTEXT_GUNICORN_CONF}
echo 'FASTTEXT_CONFIG:'${FASTTEXT_CONFIG}

exec "$@"