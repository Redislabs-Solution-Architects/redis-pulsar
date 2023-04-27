#!/bin/bash
# Maker: Joey Whelan
# Usage: sink.sh
# Description:  Utility to manage Pulsar sink operations

case $1 in
    create)
        docker exec pulsar /pulsar/bin/pulsar-admin --admin-url http://localhost:8080 \
        sinks create \
        --tenant public \
        --namespace default \
        --sink-type rjson_sink_connector \
        --name rjsonSink \
        --sink-config '{"redisHost": "192.168.20.2","redisPort": "6379","redisUser": "default","redisPassword": ""}' \
        --inputs redis-topic
        ;;
    status)
        docker exec pulsar /pulsar/bin/pulsar-admin --admin-url http://localhost:8080 \
        sinks status \
        --name rjsonSink
        ;;
    *)  
        echo "Usage: sink.sh <cmd type: create|status>" 1>&2
        exit 1
        ;;
esac