package com.yetong.service;

import com.yetong.dto.Result;
import com.yetong.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    Result getShopInfoByMutual(Long id);

    Result updateShop(Shop shop);

    Result getShopInfoByLogic(Long id);
}
