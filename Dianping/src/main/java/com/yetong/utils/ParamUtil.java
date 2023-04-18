package com.yetong.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;

import java.util.HashMap;
import java.util.Map;

public class ParamUtil {


    public static Map beanToMap(Object t) {
        Map map = BeanUtil.beanToMap(t, new HashMap(), CopyOptions.create()
                .setFieldValueEditor((name, value) -> {
                    if (value != null) {
                        return value.toString();
                    }
                    return null;
                }));
        return map;
    }

}
