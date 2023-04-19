package com.yetong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yetong.commond.RedisService;
import com.yetong.dto.Result;
import com.yetong.entity.SeckillVoucher;
import com.yetong.entity.Voucher;
import com.yetong.mapper.VoucherMapper;
import com.yetong.service.ISeckillVoucherService;
import com.yetong.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Autowired
    private RedisService redisService;

    private static final String SKILL = "skill:";

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        redisService.set(SKILL + voucher.getId(), String.valueOf(seckillVoucher.getStock()));
    }
}
