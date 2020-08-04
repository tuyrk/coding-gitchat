package com.lynn;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class Aliyun {
    private String appKey;
    private String appSecret;
    private String bucket;
    private String endPoint;
}
