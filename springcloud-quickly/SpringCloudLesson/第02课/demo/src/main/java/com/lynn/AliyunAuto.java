package com.lynn;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class AliyunAuto {
    private String appKey;
    private String appSecret;
    private String bucket;
    private String endPoint;
}
