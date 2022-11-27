package com.ssl.note.service;

import com.ssl.note.lock.RedisDistLock;
import com.ssl.note.mapper.ShopGoodsMapper;
import com.ssl.note.model.ShopGoods;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;


/**
 * @author 李瑾老师
 * <p>
 * 类说明：订单相关的服务
 */
@Service
@Transactional
public class GoodsServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(GoodsServiceImpl.class);

    @Autowired
    private ShopGoodsMapper shopGoodsMapper;

    @Autowired
    private RedisDistLock redisDistLock;

    @Autowired
    private Redisson redisson;

    public int updateGoods(long goodsId, int goodsNumber) {
        // 分布式锁
        RLock lock = redisson.getLock("RD-LOCK");
        lock.lock(10, TimeUnit.SECONDS);

        try {
            ShopGoods shopGoods = shopGoodsMapper.selectByPrimaryKey(goodsId);
            Integer goodnumber = shopGoods.getGoodsNumber() - goodsNumber;
            shopGoods.setGoodsNumber(goodnumber);
            if (shopGoodsMapper.updateByPrimaryKey(shopGoods) >= 0) {
                logger.info("修改库存成功，当前库存：[" + goodnumber + "]");
                return 1;
            } else {
                logger.error("修改库存失败，goodsId：[" + goodsId + "]");
                return -1;
            }
        } finally {
            // 问题：避免解锁太快，把别人给解锁了
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }


}
