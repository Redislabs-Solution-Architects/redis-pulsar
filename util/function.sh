#!/bin/bash
# Maker: Joey Whelan
# Usage: function.sh
# Description:  Utility to manage Pulsar Function operations

case $1 in
    create)
        docker exec pulsar /pulsar/bin/pulsar-admin --admin-url http://localhost:8080 \
        functions create \
        --function-type normalizer_function \
        --tenant public \
        --namespace default \
        --name normalizer \
        --inputs ingress-topic \
        --output redis-topic \
        --auto-ack true
        ;;
    status)
        docker exec pulsar /pulsar/bin/pulsar-admin --admin-url http://localhost:8080 \
        functions status \
        --name normalizer \
        --tenant public \
        --namespace default
        ;;
    *)  
        echo "Usage: function.sh <cmd type: create|status>" 1>&2
        exit 1
        ;;
esac