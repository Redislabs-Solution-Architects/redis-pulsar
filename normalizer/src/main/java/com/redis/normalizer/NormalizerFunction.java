package com.redis.normalizer;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

/*
 * Implements a custom Pulsar function for manipulating metric data passed as JSON.
 */
public class NormalizerFunction implements Function<byte[], byte[]> {
    private double M1_MIN = 5.0;
    private double M1_MAX = 50.0;
    private double M2_CONV = 133.0;

    /*
     * This function accepts a JSON object that is encoded as a byte array.  One field in that object represents one
     * of three numeric metric types for the given stock symbol.  This function performs some contrived scaling 
     * operations on that metric and then writes new JSON object (as a byte array) to a new topic that is being monitored
     * by a Pulsar Sink that subsequently writes the JSON to Redis.
     * 
     * @param input     JSON object containing a Redis key name, JSON path, and value object representing a
     *                  a stock ticker symbol (string) and a metric (numeric).
     * @return          Modified JSON object encoded as a byte array.
     */
    @Override
    public byte[] process(byte[] input, Context context) {
        JSONObject in = new JSONObject(new String(input, StandardCharsets.UTF_8));
        String key = in.getString("key");
        String path = in.getString("path");
        JSONObject value = in.getJSONObject("value");
        String symbol = value.getString("symbol");
        
        JSONObject val = new JSONObject();
        String metric = key.split(":")[2];
        switch (metric) {   //3 types of metrics, each with their own contrived scaling transformation
            case "M1":
                double m1 = (double) (value.getInt("M1") - M1_MIN)/(M1_MAX - M1_MIN);
                m1 = Math.round(m1 * 100.0)/100.0;
                val.put("M1", m1);
                break;
            case "M2":
                double m2 = value.getDouble("M2") / M2_CONV;
                m2 = Math.round(m2 * 100.0)/100.0;
                val.put("M2", m2);
                break;
            case "M3":
                int m3 = value.getInt("M3");
                val.put("M3", m3);
                break;
        }       
        val.put("symbol", symbol);
        JSONObject out = new JSONObject();
        out.put("key", key);
        out.put("path", path);
        out.put("value", val);
        context.getLogger().info("Writing - key: " + key + " value: " + val + " path: " + path);
        return out.toString().getBytes(StandardCharsets.UTF_8);  //write out the JSON object, as byte array, to next step in pipeline
    }  
}