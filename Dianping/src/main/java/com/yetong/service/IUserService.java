package com.yetong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yetong.dto.LoginFormDTO;
import com.yetong.dto.Result;
import com.yetong.entity.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone);

    Result login(LoginFormDTO loginForm);

    Result getUserInfo();
}
