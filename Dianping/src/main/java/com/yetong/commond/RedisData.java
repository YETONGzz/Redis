package com.yetong.commond;

import java.time.LocalDateTime;

/**
 * Redis逻辑过期数据类
 */
public class RedisData {

    private Object data;

    private LocalDateTime expireTime;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime date) {
        this.expireTime = date;
    }
}
