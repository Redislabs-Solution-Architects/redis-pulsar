#!/bin/bash
# Maker: Joey Whelan
# Usage: send.sh
# Description:  Sends a sample JSON object to Pulsar.  If things are working correctly, that object
# should be replicated in Redis via the rjsonsink connector.

PULSAR_HOME=/home/joeywhelan/apache-pulsar-2.11.0

$PULSAR_HOME/bin/pulsar-client --url pulsar://localhost:6650 \
   produce --files ${PWD}/message.json -k key:1 redis-topic