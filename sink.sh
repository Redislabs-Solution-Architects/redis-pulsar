#!/bin/bash
# Maker: Joey Whelan
# Usage: sink.sh
# Description:  Utility to manage Pulsar sink operations

NAR=${PWD}/rjsonsink/target/rjsonsink-0.0.1.nar
PULSAR_HOME=/home/joeywhelan/apache-pulsar-2.11.0

case $1 in
    localrun)
        $PULSAR_HOME/bin/pulsar-admin --admin-url http://localhost:8080 \
        sinks localrun \
        --archive $NAR \
        --tenant public \
        --namespace default \
        --name rjsonSink \
        --sink-config-file ${PWD}/rjsonsink.yml \
        --inputs redis-topic \
        --parallelism 1
        ;;
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
    status|delete|start|stop)
        $PULSAR_HOME/bin/pulsar-admin --admin-url http://localhost:8080 \
        sinks $1 \
        --name rjsonSink
        ;;
    *)  
        echo "Usage: sink.sh <cmd type: postgres|mysql|sqlserver|oracle_lm|oracle_xs>" 1>&2
        exit 1
        ;;
esac