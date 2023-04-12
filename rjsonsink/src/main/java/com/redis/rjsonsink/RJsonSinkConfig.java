package com.redis.rjsonsink;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.pulsar.io.core.annotations.FieldDoc;

@Data
@Accessors(chain = true)
public class RJsonSinkConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @FieldDoc(
        required = true,
        defaultValue = "",
        help = "Host name or IP address of Redis Host"
    )
    private String redisHost;

    @FieldDoc(
        required = true,
        defaultValue = "0",
        help = "TCP Port of Redis Host"
    )
    private int redisPort;

    @FieldDoc(
        required = false,
        defaultValue = "default",
        help = "Redis user name"
    )
    private String redisUser;

    @FieldDoc(
        required = true,
        defaultValue = "",
        help = "Redis user password"
    )
    private String redisPassword;

    public static RJsonSinkConfig load(Map<String, Object> config) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new ObjectMapper().writeValueAsString(config), RJsonSinkConfig.class);
    }

    public void validate() {
        if (redisHost == null) {
            throw new IllegalArgumentException("redisHost is not set");
        }

        if (redisPort <= 0 || redisPort > 65535) {
            throw new IllegalArgumentException("redisPort is not set");
        }

        if (redisPassword == null) {
            throw new IllegalArgumentException("redisPassword is not set");
        }
    }
    
}
