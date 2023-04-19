package com.yetong.commond;

import java.util.concurrent.TimeUnit;

public class CacheConstants {

    public static final String CODE_CACHE_PREFIX = "user:code:";
    public static final Long CODE_TIMEOUT = 5L;
    public static final String CODE_FAIL  = "验证码错误";
    public static final String SHOP_CACHE = "shop:";
    public static final Long KEY_TIMEOUT = 30L;
    public static final Long BREAK_DOWN_KEY_TIMEOUT = 3L;
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long WAIT_UNLOCK_TIME = 100L;
    public static final String CACHE_BREAK_KEY = "breakKey";
    public static final String CACHE_BREAK = "-1";
    public static final Long LOGIC_TIMEOUT = 30L;
    public static final TimeUnit PENETRATE_TIMEUNIT = TimeUnit.MINUTES;
    public static final Long LOCK_TIMEOUT = 10L;
    public static final String ID_PREFIX = "id:";
}
