package com.ssl.note.service;

import com.ssl.note.lock.RedisDistLock;
import com.ssl.note.mapper.ShopGoodsMapper;
import com.ssl.note.model.ShopGoods;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

//    @Autowired
//    private RedissonClient redissonClient;

    public int updateGoods(long goodsId, int goodsNumber) {
        // 分布式锁
//        RLock lock = redissonClient.getLock("RD-LOCK");
//        lock.lock(1, TimeUnit.SECONDS);

        try {
            redisDistLock.lock();
            ShopGoods shopGoods = shopGoodsMapper.selectByPrimaryKey(goodsId);
            Integer goodnumber = shopGoods.getGoodsNumber() - goodsNumber;
            shopGoods.setGoodsNumber(goodnumber);
            if (shopGoodsMapper.updateByPrimaryKey(shopGoods) >= 0) {
                //logger.info("修改库存成功：[" + orderId + "]");
                return 1;
            } else {
                logger.error("修改库存失败，goodsId：[" + goodsId + "]");
                return -1;
            }
        } finally {
            redisDistLock.unlock();
        }

    }


}
