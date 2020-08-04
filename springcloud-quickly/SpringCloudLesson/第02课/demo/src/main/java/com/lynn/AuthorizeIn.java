package com.lynn;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthorizeIn {
    @NotBlank(message = "缺少response_type参数")
    private String responseType;
    @NotBlank(message = "缺少client_id参数")
    private String ClientId;
    private String state;
    @NotBlank(message = "缺少redirect_uri参数")
    private String redirectUri;
}
