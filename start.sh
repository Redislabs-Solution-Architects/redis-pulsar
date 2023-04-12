#!/bin/bash
# Maker: Joey Whelan
# Usage: start.sh
# Description:  Starts up a 1-node Redis Enterpise cluster + Pulsar standalone

GEARS=redisgears_python.Linux-ubuntu18.04-x86_64.1.2.6.zip
JSON=rejson.Linux-ubuntu18.04-x86_64.2.4.6.zip
SEARCH=redisearch.Linux-ubuntu18.04-x86_64.2.6.6.zip

gears_check() {
    while [ -z "$(curl -s -k -u "redis@redis.com:redis" https://localhost:9443/v1/modules | \
    jq '.[] | select(.display_name=="RedisGears").semantic_version')" ]
    do  
        sleep 3
    done
}

if [ ! -f $JSON ]
then
    echo "*** Fetch JSON module  ***"
    wget -q https://redismodules.s3.amazonaws.com/rejson/$JSON
fi 

if [ ! -f $SEARCH ]
then
    echo "*** Fetch SEARCH module  ***"
    wget -q https://redismodules.s3.amazonaws.com/redisearch/$SEARCH
fi 

if [ ! -f $GEARS ]
then
    echo "*** Fetch Gears  ***"
    wget -q https://redismodules.s3.amazonaws.com/redisgears/$GEARS 
fi

echo "*** Launch Redis Enterprise + Pulsar Containers ***"
docker compose up -d

echo "*** Wait for Redis Enterprise to come up ***"
curl -s -o /dev/null --retry 5 --retry-all-errors --retry-delay 3 -f -k -u "redis@redis.com:redis" https://localhost:9443/v1/bootstrap

echo "*** Build Cluster ***"
docker exec -it re1 /opt/redislabs/bin/rladmin cluster create name cluster.local username redis@redis.com password redis

echo "*** Load Modules ***"
curl -s -o /dev/null -k -u "redis@redis.com:redis" https://localhost:9443/v2/modules -F module=@$GEARS
curl -s -o /dev/null -k -u "redis@redis.com:redis" https://localhost:9443/v1/modules -F module=@$JSON
curl -s -o /dev/null -k -u "redis@redis.com:redis" https://localhost:9443/v1/modules -F module=@$SEARCH

echo "*** Wait for Gears Module to load ***"
gears_check

echo "*** Build Target Redis DB ***"
curl -s -o /dev/null -k -u "redis@redis.com:redis" https://localhost:9443/v1/bdbs -H "Content-Type:application/json" -d @targetdb.json

echo "*** Create RJson Sink ***"
./sink.sh create

echo "*** Send sample JSON message to Pulsar ***"
./send.sh