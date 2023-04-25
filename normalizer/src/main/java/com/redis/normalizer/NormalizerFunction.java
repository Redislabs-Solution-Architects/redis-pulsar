package com.redis.normalizer;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class NormalizerFunction implements Function<byte[], byte[]> {
    private double M1_MIN = 5.0;
    private double M1_MAX = 50.0;
    private double M2_CONV = 133.0;

    @Override
    public byte[] process(byte[] input, Context context) {
        JSONObject in = new JSONObject(new String(input, StandardCharsets.UTF_8));
        String key = in.getString("key");
        String path = in.getString("path");
        JSONObject value = in.getJSONObject("value");
        String symbol = value.getString("symbol");
        
        JSONObject val = new JSONObject();
        String metric = key.split(":")[2];
        switch (metric) {
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
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }  
}