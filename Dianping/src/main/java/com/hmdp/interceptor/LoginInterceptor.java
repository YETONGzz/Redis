package com.hmdp.interceptor;

import com.hmdp.commond.UserHolder;
import com.hmdp.utils.ResponseUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class LoginInterceptor implements HandlerInterceptor {


    /**
     * 校验用户是否登陆,controller之前执行
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //判断ThreadLocal中有无用户
        if (UserHolder.getUser() == null) {
            ResponseUtil.responseJson(response, ResponseUtil.response(403, "请登录系统", null));
            return false;
        }
        return true;
    }


    /**
     * 视图渲染之后执行
     * 销毁保存在ThreadLocal中的用户信息
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
