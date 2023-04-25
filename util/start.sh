#!/bin/bash
# Maker: Joey Whelan
# Usage: start.sh
# Description:  Starts up a Redis 7.2RC instance + Pulsar standalone

echo "*** Launch Redis + Pulsar Containers ***"
docker compose up -d

echo "*** Wait for Pulsar to start ***"
sleep 20

echo "*** Create RJson Sink ***"
./sink.sh create

echo "*** Create Normalizer Function ***"
./function.sh create

echo "*** Build and Deploy Redis Gear ***"
cd ../gear && npm run build && npm run deploy && cd -
