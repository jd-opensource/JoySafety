#!/bin/sh
set -ex

cd /work
echo 'KG_CONFIG_FILE:'${KG_CONFIG_FILE}

exec "$@"