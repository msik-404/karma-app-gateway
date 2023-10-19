#! /usr/bin/bash

# stop containers
DIR=$(dirname $BASH_SOURCE)
source "$DIR/stop.sh"

# delete containers
docker rm karma-app-gateway-backend-1 && docker rm karma-app-gateway-redis-1

# deleted backend image
docker image rm karma-app-gateway-backend

# delete volumes
docker volume rm karma-app-gateway_redis-data