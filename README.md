# Redis Pulsar Integration 

## Contents
1.  [Summary](#summary)
2.  [Features](#features)
3.  [Prerequisites](#prerequisites)
4.  [Installation](#installation)
5.  [Usage](#usage)

## Summary <a name="summary"></a>
This contains Java source for a custom Pulsar sink + utilities to build a complete Redis + Pulsar environment.

## Features <a name="features"></a>
- Java source to create a custom Apache Pulsar sink.  That sink connects to Redis and writes to Redis JSON.
- Docker compose file to start up a 1-node Redis cluster + Pulsar standalone instance.
- Shell script to orchestrate cluster + db build on Redis and custom sink installation on Pulsar standalone.

## Prerequisites <a name="prerequisites"></a>
- Ubuntu 20.x or higher
- Docker
- Command line tools: curl, wget
- Java

## Installation <a name="installation"></a>
```bash
git clone https://github.com/Redislabs-Solution-Architects/redis-pulsar.git && cd redis-pulsar
```

## Usage <a name="usage"></a>

### Execution
```bash
./start.sh
```
```bash
./stop.sh
```
 
