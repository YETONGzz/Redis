package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheService;
import com.hmdp.utils.ParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.commond.CacheConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    CacheService cacheService;

    /**
     * 获取店铺信息
     *
     * @param id
     * @return
     */
    @Override
    public Result getShopInfo(Long id) {
        //如果没有获取到锁等待一会重新执行该方法
        if (!cacheService.tryLock(LOCK_SHOP_KEY + id)) {
            Thread.sleep(WAIT_UNLOCK_TIME);
            getShopInfo(id);
        }
        String shopKey = SHOP_CACHE + id;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(shopKey);
        if (!CollectionUtils.isEmpty(entries)) {
            return Result.ok(entries);
        }
        Shop info = query().eq("id", id).one();
        if (info == null) {
            //通过缓存空对象解决缓存穿透
            redisTemplate.opsForHash().putAll(shopKey, null);
            redisTemplate.expire(shopKey, BREAK_DOWN_KEY_TIMEOUT, TimeUnit.MINUTES);
            return Result.fail("不存在该店铺信息");
        }
        Map map = ParamUtil.beanToMap(info);
        redisTemplate.opsForHash().putAll(shopKey, map);
        redisTemplate.expire(shopKey, KEY_TIMEOUT, TimeUnit.MINUTES);
        return Result.ok(info);
    }


    /**
     * 更新店铺信息
     * 先更数据库后删除缓存可以更好地防止redis和数据库数据不一致的情况
     *
     * @param shop
     * @return
     */
    @Override
    public Result updateShop(Shop shop) {
        updateById(shop);
        redisTemplate.delete(SHOP_CACHE + shop.getId());
        return Result.ok();
    }


}
