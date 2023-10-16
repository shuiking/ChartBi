package com.lk.analyze.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求体
 * @Author : lk
 * @create 2023/10/14
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
