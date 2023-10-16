package com.lk.analyze.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登陆请求体
 * @Author : lk
 * @create 2023/10/14
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;
}
