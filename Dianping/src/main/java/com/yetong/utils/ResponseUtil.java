package com.yetong.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class ResponseUtil {

    /**
     * 返回编码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String msg;

    /**
     * 返回数据
     */
    private Object data;

    public ResponseUtil(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 获取request
     */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取response
     */
    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取session
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    /**
     * 返回信息
     *
     * @param code 返回编码
     * @param msg  返回消息
     * @param data 返回数据
     * @return
     */
    public static ResponseUtil response(Integer code, String msg, Object data) {
        return new ResponseUtil(code, msg, data);
    }

    /**
     * 将data渲染到客户端
     *
     * @param response
     * @param status
     * @param data
     */
    public static void responseJson(HttpServletResponse response,  Object data) {
        try {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "*");
            response.setContentType("application/json;charset=UTF-8");
            String s = new ObjectMapper().writeValueAsString(data);
            response.getWriter().write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
