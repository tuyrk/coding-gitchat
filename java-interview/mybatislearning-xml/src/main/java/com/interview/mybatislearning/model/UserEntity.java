package com.interview.mybatislearning.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserEntity implements Serializable {
    private static final long serialVersionUID = -5980266333958177104L;
    private Integer id;
    private String userName;
    private String passWord;
    private String nickName;

    public UserEntity(String userName, String passWord, String nickName) {
        this.userName = userName;
        this.passWord = passWord;
        this.nickName = nickName;
    }
}
