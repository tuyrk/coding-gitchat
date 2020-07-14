package com.lynn.index.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 私钥输入参数（其实就是客户端通过服务端返回的公钥加密后的客户端自己生成的公钥）
 */
@Data
public class KeyRequest {
    /**
     * 客户端自己生成的加密后公钥
     */
    @NotNull
    private String clientEncryptPublicKey;
}
