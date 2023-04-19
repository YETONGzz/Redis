package com.yetong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yetong.commond.RedisIdWorker;
import com.yetong.commond.RedisService;
import com.yetong.commond.UserHolder;
import com.yetong.dto.Result;
import com.yetong.entity.SeckillVoucher;
import com.yetong.entity.VoucherOrder;
import com.yetong.mapper.VoucherOrderMapper;
import com.yetong.service.ISeckillVoucherService;
import com.yetong.service.IVoucherOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * 秒杀优惠券
     *
     * @param voucherId
     * @return
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
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
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock -1")
                .eq("voucher_id", seckillVoucher.getVoucherId())
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
        Long userId = UserHolder.getUser().getId();
        voucherOrder.setUserId(userId);
        // 6.3.代金券id
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);

        return null;
    }
}
