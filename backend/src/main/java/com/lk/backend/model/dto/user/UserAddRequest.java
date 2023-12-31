package com.lk.backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求体
 * @Author : lk
 * @create 2023/10/14
 */
@Data
public class UserAddRequest implements Serializable {
    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
