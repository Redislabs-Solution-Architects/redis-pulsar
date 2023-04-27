# Redis Pulsar Integration - Continuous Query Updates

## Contents
1.  [Summary](#summary)
2.  [Architecture](#architecture)
3.  [Data Flow](#dataflow)
4.  [Features](#features)
5.  [Prerequisites](#prerequisites)
6.  [Installation](#installation)
7.  [Usage](#usage)
8.  [Execution](#execution)

## Summary <a name="summary"></a>
This implements a continous query update architecture by integrating Apache Pulsar, Redis Gears, Redis Search, and Redis Search.  The data transfers and transforms simulate a stock trading desk domain.

## Architecture <a name="architecture"></a>
![architecture](https://docs.google.com/drawings/d/e/2PACX-1vQyBS3608xN_wTwZfVI2feoOQ4soHbe3No7Dkukfq0iJfYyXvhuyU1p0BwLpqOgDmE0w9AV1u6DoWN1/pub?w=663&h=380)
## Data Flow <a name="dataflow"></a>
![dataflow](https://docs.google.com/drawings/d/e/2PACX-1vQ9Tov-JyYsrlHsVrj5LPmznM2J7I2PiHgOmgGR3JyRrm-JAKsK08no-Wk4-SgZMYbDmr7QF-cDnjGS/pub?w=669&h=275)

## Features <a name="features"></a>
- Java source to create a custom Apache Pulsar sink.  That sink connects to Redis and writes to Redis JSON.
- Java source to create a custom Apache Pulsar function.  That function provides scaling/normalization of ingress metric data.
- Nodejs source to implement an Apache Pulsar producer.
- Nodejs source to implement a Redis Streams consumer.
- Nodejs source to implement a Redis Gears 2.0 function that monitors for key space events and then performs aggregations that are subsequently written to a Redis Stream.
- Docker compose file to start up a 1-node Redis cluster + Pulsar standalone instance.
- Shell script to orchestrate cluster + db build on Redis, custom sink + function installation on Pulsar standalone, and Redis Gears 2.0 function build and deployment.

## Prerequisites <a name="prerequisites"></a>
- Ubuntu 20.x or higher
- Docker
- Java
- Nodejs

## Installation <a name="installation"></a>
```bash
git clone https://github.com/Redislabs-Solution-Architects/redis-pulsar.git && cd redis-pulsar/util
```

## Usage <a name="usage"></a>

### Compile Source
```bash
./build.sh
```

### Pulsar + Redis start up
```bash
./start.sh
```

### Redis Stream Consumer start up - separate terminal session
```bash
cd ../consumer && npm start
```

### Pulsar Producer start up - separate terminal session
```bash
cd ../producer && npm start
```

### Shut down
```bash
./stop.sh
```

## Execution <a name="execution"></a>
### Producer 
 ```bash
> producer@1.0.0 start
> node ./src/producer.js

sending: {"key":"doc:AA:M2:0","path":"$","value":{"symbol":"AA","M2":561061.12}}
sending: {"key":"doc:CC:M1:1","path":"$","value":{"symbol":"CC","M1":26.28}}
sending: {"key":"doc:AA:M3:2","path":"$","value":{"symbol":"AA","M3":73}}
sending: {"key":"doc:AA:M3:3","path":"$","value":{"symbol":"AA","M3":281}}
sending: {"key":"doc:AA:M1:4","path":"$","value":{"symbol":"AA","M1":38.9}}
sending: {"key":"doc:BB:M3:5","path":"$","value":{"symbol":"BB","M3":860}}
sending: {"key":"doc:BB:M3:6","path":"$","value":{"symbol":"BB","M3":910}}
sending: {"key":"doc:BB:M2:7","path":"$","value":{"symbol":"BB","M2":437042.42}}
sending: {"key":"doc:CC:M2:8","path":"$","value":{"symbol":"CC","M2":707502.17}}
sending: {"key":"doc:DD:M1:9","path":"$","value":{"symbol":"DD","M1":27.68}}
 ```

 ### Consumer
 ```bash
> consumer@1.0.0 start
> node ./src/consumer.js

stream client awaiting messages
{"id":"1682458331763-0","message":{"m2_sum":"{ \"symbol\": \"AA\", \"m2_sum\": \"4218.5\" }"}}
{"id":"1682458332718-0","message":{"m1_ave":"{ \"symbol\": \"CC\", \"m1_ave\": \"0.47\" }"}}
{"id":"1682458333744-0","message":{"m3_p99":"{ \"symbol\": \"AA\", \"m3_p99\": \"73\" }"}}
{"id":"1682458334754-0","message":{"m3_p99":"{ \"symbol\": \"AA\", \"m3_p99\": \"281\" }"}}
{"id":"1682458335778-0","message":{"m1_ave":"{ \"symbol\": \"AA\", \"m1_ave\": \"0.1825\" }"}}
{"id":"1682458336788-0","message":{"m3_p99":"{ \"symbol\": \"BB\", \"m3_p99\": \"860\" }"}}
{"id":"1682458337800-0","message":{"m3_p99":"{ \"symbol\": \"BB\", \"m3_p99\": \"910\" }"}}
{"id":"1682458338824-0","message":{"m2_sum":"{ \"symbol\": \"BB\", \"m2_sum\": \"3286.03\" }"}}
{"id":"1682458339834-0","message":{"m2_sum":"{ \"symbol\": \"CC\", \"m2_sum\": \"5319.57\" }"}}
{"id":"1682458340860-0","message":{"m1_ave":"{ \"symbol\": \"DD\", \"m1_ave\": \"0.49\" }"}}
 ```
