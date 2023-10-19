package com.lk.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lk.backend.model.dto.user.UserQueryRequest;
import com.lk.backend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lk.backend.model.vo.LoginUserVo;
import com.lk.backend.model.vo.UserVo;
import com.lk.common.model.to.UserTo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author k
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2023-10-14 21:32:17
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVo getLoginUserVo(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(UserTo user);



    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVo getUserVo(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVo> getUserVo(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

}
