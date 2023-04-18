package com.yetong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yetong.commond.UserHolder;
import com.yetong.dto.LoginFormDTO;
import com.yetong.dto.Result;
import com.yetong.dto.UserDTO;
import com.yetong.entity.User;
import com.yetong.mapper.UserMapper;
import com.yetong.service.IUserService;
import com.yetong.utils.JWTUtil;
import com.yetong.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yetong.commond.CacheConstants.*;
import static com.yetong.commond.Constants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    JWTUtil jwtUtil;

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 发送手机验证码
     *
     * @param phone
     * @return
     */
    @Override
    public Result sendCode(String phone) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail(PHONE_PATTERN_FAIL);
        }
        String code = RandomUtil.randomNumbers(CODE_LENGTH);
        redisTemplate.opsForValue().set(CODE_CACHE_PREFIX + phone, code, CODE_TIMEOUT, TimeUnit.MINUTES);
        log.info("验证码:" + code);
        return Result.ok();
    }


    /**
     * 用户登陆
     *
     * @param loginForm
     * @return
     */
    @Override
    public Result login(LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail(PHONE_PATTERN_FAIL);
        }
        if (!StringUtils.hasText(code)) {
            return Result.fail(CODE_NOT_INPUT);
        }
        String cacheCode = "";
        if (!StringUtils.hasText(cacheCode = redisTemplate.opsForValue().get(CODE_CACHE_PREFIX + phone))) {
            return Result.fail(CODE_FAIL);
        }
        if (!code.equals(cacheCode)) {
            return Result.fail(CODE_FAIL);
        }
        User user = query().eq("phone", phone).one();
        if (user == null) {
            user = saveUser(phone);
        }
        String accessToken = jwtUtil.createAccessToken(user);
        String key = CODE_CACHE_PREFIX + accessToken;
        Map map = BeanUtil.beanToMap(user, new HashMap(), CopyOptions.create()
                .setIgnoreNullValue(true)
                .setFieldValueEditor((name, value) -> value.toString()));
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, TOKEN_TIMEOUT, TimeUnit.MINUTES);
        return Result.ok(accessToken);
    }

    @Override
    public Result getUserInfo() {
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    /**
     * 保存用户
     *
     * @param phone
     */
    private User saveUser(String phone) {
        User user = new User();
        user.setId(Long.valueOf(RandomUtil.randomNumbers(10)));
        user.setPhone(phone);
        user.setNickName(USER_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
