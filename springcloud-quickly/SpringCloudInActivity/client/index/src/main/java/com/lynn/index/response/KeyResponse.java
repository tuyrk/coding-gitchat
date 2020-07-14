package com.lynn.index.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 私钥输出参数
 */
@Getter
@Builder
public class KeyResponse extends BaseResponse {
    /**
     * 整个系统所有加密算法共用的密钥
     */
    private String key;
}
