package com.yetong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yetong.commond.CacheConstants;
import com.yetong.commond.RedisIdWorker;
import com.yetong.commond.RedisService;
import com.yetong.commond.UserHolder;
import com.yetong.dto.Result;
import com.yetong.entity.SeckillVoucher;
import com.yetong.entity.VoucherOrder;
import com.yetong.mapper.VoucherOrderMapper;
import com.yetong.service.ISeckillVoucherService;
import com.yetong.service.IVoucherOrderService;
import com.yetong.utils.SimpleRedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    RedisService redisService;
    @Autowired
    ISeckillVoucherService seckillVoucherService;
    @Autowired
    RedisIdWorker idWorker;
    @Autowired
    IVoucherOrderService voucherOrderService;
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 秒杀优惠券
     *
     * @param voucherId
     * @return
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        if (LocalDateTime.now().isBefore(seckillVoucher.getBeginTime())) {
            return Result.fail("秒杀尚未开始");
        }
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 尚未开始
            return Result.fail("秒杀已经结束！");
        }
        if (seckillVoucher.getStock() < 1) {
            return Result.fail("可用优惠券不足");
        }

        /**
         * 因为事务是由 spring自动管理 所以如果锁住里面的内容可能会出现锁已经释放 但是事务并未提交的情况
         * 这样就会造成线程安全问题 因为事务未提交 所以其他线程不知道用户有没有已经抢到这张券了
         */

//        synchronized (userId.toString().intern()) {
//            VoucherOrder order = query()
//                    .eq("user_id", UserHolder.getUser().getId())
//                    .eq("voucher_id", voucherId).one();
//            if (order != null) {
//                return Result.fail("用户已经抢过该秒杀券");
//            }
//
////        SeckillVoucher info = seckillVoucherService.getById(voucherId);
//
//            //使用乐观锁 解决并发线程安全问题 库存充当版本号 但是这样会存在当很多个进程同时操作时失败次数过多的问题
//            //当前的场景只要保证数量》0即可所以不适合使用乐观锁 因为乐观锁认为只要数据变化了 那么就不允许继续执行了
////        boolean success = seckillVoucherService.update()
////                .setSql("stock = stock -1")
////                .eq("voucher_id", seckillVoucher.getVoucherId())
////                .eq("stock",info.getStock())
////                .update();
//
//            //只需要判断库存是否大于0即可
//            boolean success = seckillVoucherService.update()
//                    .setSql("stock = stock -1")
//                    .gt("stock", 0)
//                    .update();
//            if (!success) {
//                return Result.fail("库存不足");
//            }
//            //6.创建订单
//            VoucherOrder voucherOrder = new VoucherOrder();
//            // 6.1.订单id
//            orderId = idWorker.nextId("order");
//            voucherOrder.setId(orderId);
//            // 6.2.用户id
//            voucherOrder.setUserId(userId);
//            // 6.3.代金券id
//            voucherOrder.setVoucherId(voucherId);
//            save(voucherOrder);
//        }
        SimpleRedisLock redisLock = SimpleRedisLock.getRedisLock(CacheConstants.VOUCHER_LOCK + userId, redisTemplate);

        if (!redisLock.tryReentrantLock(CacheConstants.VOUCHER_LOCK_TIMEOUT)) {
            return Result.fail("不允许重复下单");
        }
        test(redisLock);
        try {
            return voucherOrderService.createOrder(voucherId);
        } catch (Exception e) {
            return Result.fail("创建订单失败");
        } finally {
            redisLock.unReentrantLock();
        }
    }

    @Transactional
    @Override
    public Result createOrder(long voucherId) {
        Long userId = UserHolder.getUser().getId();
        VoucherOrder order = query()
                .eq("user_id", UserHolder.getUser().getId())
                .eq("voucher_id", voucherId).one();
        if (order != null) {
            return Result.fail("用户已经抢过该秒杀券");
        }

        //只需要判断库存是否大于0即可
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock -1")
                .gt("stock", 0)
                .update();
        if (!success) {
            return Result.fail("库存不足");
        }
        //6.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 6.1.订单id
        long orderId = idWorker.nextId("order");
        voucherOrder.setId(orderId);
        // 6.2.用户id
        voucherOrder.setUserId(userId);
        // 6.3.代金券id
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);
        return Result.ok(orderId);
    }

    public void test(SimpleRedisLock redisLock) {
        try {
            boolean b = redisLock.tryReentrantLock(CacheConstants.VOUCHER_LOCK_TIMEOUT);
            System.out.println(b);
        }catch (Exception e){

        }finally {
            redisLock.unReentrantLock();
        }

    }
}
