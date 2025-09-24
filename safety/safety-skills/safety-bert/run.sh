#!/bin/sh
set -ex

cd /work
echo 'BERT_CONFIG_FILE:'${BERT_CONFIG_FILE}

exec "$@"