package com.lynn.index.response;

import lombok.Builder;
import lombok.Getter;

/**
 * RSA生成的公私钥输出参数
 */
@Getter
@Builder
public class RSAResponse extends BaseResponse {
    private String serverPublicKey;
    private String serverPrivateKey;
}
