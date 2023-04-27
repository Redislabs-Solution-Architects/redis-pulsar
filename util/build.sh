#!/bin/bash
# Maker: Joey Whelan
# Usage: build.sh
# Description:  Utility to build/compile sources

cd ../normalizer && mvn clean package && cd -
cd ../rjsonsink && mvn clean package && cd -
cd ../gear && npm run build && cd -