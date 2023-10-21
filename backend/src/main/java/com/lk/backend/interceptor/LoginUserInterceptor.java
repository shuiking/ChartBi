package com.lk.backend.interceptor;

import cn.hutool.core.text.AntPathMatcher;
import com.lk.backend.model.entity.User;
import com.lk.common.model.to.UserTo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.lk.common.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户登陆拦截器
 * @Author : lk
 * @create 2023/10/19
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserTo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        boolean match1 = new AntPathMatcher().match("/user/login", uri);
        boolean match2 = new AntPathMatcher().match("/user/register", uri);
        boolean match3=new AntPathMatcher().match("/alipay/notify",uri);

        if (match1||match2||match3) {
            return true;
        }
//        return true;

        HttpSession session = request.getSession();
        UserTo user = (UserTo) session.getAttribute(USER_LOGIN_STATE);
        if (user != null) {
            //已登录
            loginUser.set(user);
            return true;
        }else {
            //未登陆
            session.setAttribute("msg","请先登录");
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
