package com.redis.rjsonsink;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.pulsar.io.core.Sink;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.core.SinkContext;
import org.apache.pulsar.io.core.annotations.Connector;
import org.apache.pulsar.io.core.annotations.IOType;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path2;
import org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;


/*
 * This class implements a custom Pulsar I/O sink that provides a write to Redis JSON.
 */
@Connector(
        name = "custom_sink_connector",
        type = IOType.SINK,
        help = "Redis JSON sink connector for Pulsar I/O",
        configClass = RJsonSinkConfig.class
)
@Slf4j
public class RJsonSinkConnector implements Sink<byte[]> {

    private RJsonSinkConfig sinkConfig;
    private JedisPooled client;
    private ExecutorService executor; 

    /*
     * This function uses the Jedis client lib to open a connection to Redis
     */
    @Override
    public void open(Map<String, Object> config, SinkContext sinkContext) throws Exception {
        log.info("Open rjson sink");
        sinkConfig = RJsonSinkConfig.load(config);
        sinkConfig.validate();

        /* Open Redis client connection */
        client = new JedisPooled(sinkConfig.getRedisHost(),  
            sinkConfig.getRedisPort(),
            sinkConfig.getRedisUser(),
            sinkConfig.getRedisPassword()
        );
        executor = Executors.newCachedThreadPool();  //provide async functionality for Redis writes
    }

    /*
     * This function accepts a JSON object as input then performs a Redis JSON write of that object.  The write
     * happens in a separate thread.
     * @param   record  JSON object encoded as a byte array
     */
    @Override
    public void write(Record<byte[]> record) throws Exception {
        Runnable task = () -> {
            try {
                String recordValue = new String(record.getValue(), StandardCharsets.UTF_8);
                JSONObject document = new JSONObject(recordValue);
                String key = document.getString("key");
                Path2 path = new Path2(document.getString("path"));
                JSONObject value = document.getJSONObject("value");
                log.info("Writing - key: " + key + " value: " + value + " path: " + path);
                client.jsonSet(key, path, value);
                record.ack();
            }
            catch (Exception e) {
                record.fail();
                log.warn("Sink write exception ", e);
            }
        };
        executor.execute(task);
    }

    @Override
    public void close() {
        executor.shutdown();
        client.close();
    }   
}