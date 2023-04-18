package com.yetong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yetong.commond.RedisService;
import com.yetong.dto.Result;
import com.yetong.entity.Shop;
import com.yetong.mapper.ShopMapper;
import com.yetong.service.IShopService;
import com.yetong.utils.CacheService;
import com.yetong.utils.ParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.yetong.commond.CacheConstants.*;

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
    @Autowired
    RedisService redisService;

    private static final ExecutorService CACHE_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 获取店铺信息
     * 通过互斥锁解决缓存击穿
     *
     * @param id
     * @return
     */
    @Override
    public Result getShopInfoByMutual(Long id) {
        String shopKey = SHOP_CACHE + id;
        //判断是否缓存过空对象
        if (CACHE_BREAK.equals(redisTemplate.opsForHash().get(shopKey, CACHE_BREAK_KEY))) {
            return null;
        }
        Map<java.lang.Object, Object> entries = redisTemplate.opsForHash().entries(shopKey);
        if (!CollectionUtils.isEmpty(entries)) {
            return Result.ok(entries);
        }

        //用来判断是否需要释放锁
        boolean flag = true;
        String lockKey = LOCK_SHOP_KEY + id;
        Shop info = null;

        //使用互斥锁 解决缓存击穿 如果没有获取到锁等待一会重新执行该方法
        try {
            if (!cacheService.tryLock(LOCK_SHOP_KEY + id)) {
                flag = !flag;
                Thread.sleep(WAIT_UNLOCK_TIME);
                getShopInfoByMutual(id);
            }
            info = query().eq("id", id).one();
            if (info == null) {
                //通过缓存空对象解决缓存穿透
                redisTemplate.opsForHash().put(shopKey, CACHE_BREAK_KEY, CACHE_BREAK);
                redisTemplate.expire(shopKey, BREAK_DOWN_KEY_TIMEOUT, TimeUnit.MINUTES);
                return Result.fail("不存在该店铺信息");
            }
            Map map = ParamUtil.beanToMap(info);
            redisTemplate.opsForHash().putAll(shopKey, map);
            redisTemplate.expire(shopKey, KEY_TIMEOUT, TimeUnit.MINUTES);
        } catch (Exception e) {
            return Result.fail("获取锁异常");
        } finally {
            //释放锁
            if (flag) cacheService.unLock(lockKey);
        }
        return Result.ok(info);
    }


    /**
     * 通过逻辑过期解决缓存击穿
     *
     * @param id
     * @return
     */
//    @Override
//    public Result getShopInfoByLogic(Long id) {
//        String shopKey = SHOP_CACHE + id;
//        //判断是否缓存过空对象
//        if (CACHE_BREAK.equals(redisTemplate.opsForValue().get(shopKey))) {
//            return null;
//        }
//        String jsonData = redisTemplate.opsForValue().get(shopKey);
//        //因为是逻辑过期 所以当json为空时什么也不做
//        if (!StringUtils.hasText(jsonData)) {
//            return null;
//        }
//
//        RedisData redisData = JSON.parseObject(jsonData, RedisData.class);
//        cn.hutool.json.JSONObject data = (cn.hutool.json.JSONObject) redisData.getData();
//        Shop shop = JSONUtil.toBean(data, Shop.class);
//        //没有过期直接返回
//        if (!LocalDateTime.now().isAfter(redisData.getExpireTime())) {
//            return Result.ok(shop);
//        }
//
//        String lockKey = LOCK_SHOP_KEY + id;
//        //获取锁
//        if (cacheService.tryLock(lockKey)) {
//            //获取到锁进行缓存重建
//            CACHE_EXECUTOR.submit(() -> {
//                try {
//                    Shop info = query().eq("id", id).one();
//                    if (info == null) {
//                        //通过缓存空对象解决缓存穿透
//                        redisTemplate.opsForValue().set(shopKey, CACHE_BREAK);
//                        redisTemplate.expire(shopKey, BREAK_DOWN_KEY_TIMEOUT, TimeUnit.MINUTES);
//                    }
//                    RedisData cacheData = new RedisData();
//                    cacheData.setData(info);
//                    cacheData.setExpireTime(LocalDateTime.now().plusMinutes(TimeUnit.MINUTES.toMinutes(LOGIC_TIMEOUT)));
//                    redisTemplate.opsForValue().set(shopKey, JSON.toJSONString(info));
//                }catch (Exception e){
//                    log.error("缓存重建失败",e);
//                }finally {
//                    cacheService.unLock(lockKey);
//                }
//            });
//        }
//        return Result.ok(shop);
//    }


    /**
     * 通过逻辑过期解决缓存击穿问题
     *
     * @param id
     * @return
     */
    @Override
    public Result getShopInfoByLogic(Long id) {
        String key = SHOP_CACHE + id;
        Shop info = redisService.cacheBreakByLogicExpire(key, Shop.class, () -> query().eq("id", id).one(), 10L, TimeUnit.SECONDS);
        if (info == null) {
            return Result.fail("数据不存在");
        }
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
    @Transactional
    public Result updateShop(Shop shop) {
        updateById(shop);
        redisTemplate.delete(SHOP_CACHE + shop.getId());
        return Result.ok();
    }

}
