package com.lk.backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新个人信息请求体
 * @Author : lk
 * @create 2023/10/15
 */
@Data
public class UserUpdateMyRequest implements Serializable {
    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;


    private static final long serialVersionUID = 1L;
}
