#!/bin/bash
# Maker: Joey Whelan
# Usage: sink.sh
# Description:  Utility to manage Pulsar sink operations

NAR=${PWD}/../rjsonsink/target/rjsonsink-0.0.1.nar
PULSAR_HOME=/home/joeywhelan/apache-pulsar-2.11.0

case $1 in
    create)
        $PULSAR_HOME/bin/pulsar-admin --admin-url http://localhost:8080 \
        sinks create \
        --tenant public \
        --namespace default \
        --sink-type rjson_sink_connector \
        --name rjsonSink \
        --sink-config-file ${PWD}/rjsonsink.yml \
        --inputs redis-topic
        ;;
    status)
        $PULSAR_HOME/bin/pulsar-admin --admin-url http://localhost:8080 \
        sinks status \
        --name rjsonSink
        ;;
    *)  
        echo "Usage: sink.sh <cmd type: create|status>" 1>&2
        exit 1
        ;;
esac