package com.ssl.note.controller;

import com.ssl.note.utils.RedisUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author SongShengLin
 * @date 2022/11/27 11:51
 * @description
 */
@RestController
public class GoodController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${server.port}")
    private String serverPort;

    /**
     * 分布式下，不通的jvm下，syn或者lock锁是无效的
     */
    @GetMapping("/buy_goods")
    public String buy_Goods() {
        // 分布式下，不通的jvm下，syn或者lock锁是无效的
        String result = stringRedisTemplate.opsForValue().get("goods:001");
        int goodsNumber = result == null ? 0 : Integer.parseInt(result);

        if (goodsNumber > 0) {
            int realNumber = goodsNumber - 1;
            stringRedisTemplate.opsForValue().set("goods:001", realNumber + "");
            System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件" + "\t 服务器端口：" + serverPort);
            return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件" + "\t 服务器端口：" + serverPort;
        } else {
            System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临" + "\t 服务器端口：" + serverPort);
        }

        return "商品已经售罄/活动结束/调用超时，欢迎下次光临" + "\t 服务器端口：" + serverPort;
    }


    public static final String KEY = "LOCK-KEY";

    /**
     * redis+lua实现分布式锁
     */
    @GetMapping("/buy_goods_redis")
    public String buy_Goods1() throws Exception {
        String value = null;
        try {
            value = UUID.randomUUID().toString() + Thread.currentThread();
//            Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(KEY, value);
            // 防止宕机，设置过期时间
            // 问题：但是没有原子性
//            stringRedisTemplate.expire(KEY, 10L, TimeUnit.SECONDS);

            // 问题：由于网络抖动，删除了别人的加的锁
            Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(KEY, value, 10L, TimeUnit.SECONDS);

            if (Boolean.FALSE.equals(flag)) {
                return "抢锁失败，请重试！";
            }

            // 业务代码
            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);

            if (goodsNumber > 0) {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001", realNumber + "");
                System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件" + "\t 服务器端口：" + serverPort);
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件" + "\t 服务器端口：" + serverPort;
            } else {
                System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临" + "\t 服务器端口：" + serverPort);
            }
        } finally {
            // 异常：删除锁需要在finally上，防止中途异常无法删除锁
            // 服务器宕机：无法进入了，所以需要加过期时间
            // 问题：由于网络抖动，删除了别人的加的锁,加一个判断。但是两行代码，不是原子性
//            if (stringRedisTemplate.opsForValue().get(KEY).equalsIgnoreCase(value)) {
//                stringRedisTemplate.delete(KEY);
//            }

            // 引入lua脚本，保证原子性
            // 截止目前，单机版的redis分布式锁完成
            // 但是还有问题：业务时间怎么保证一定小于过期时间？=看门狗=续约问题
            try (Jedis jedis = RedisUtils.getJedis()) {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                        "then " +
                        "return redis.call('del', KEYS[1]) " +
                        "else " +
                        "   return 0 " +
                        "end";
                Object result = jedis.eval(script, Collections.singletonList(KEY), Collections.singletonList(value));
                if ("1".equals(result.toString())) {
                    System.out.println("------del REDIS_LOCK_KEY success");
                } else {
                    System.out.println("------del REDIS_LOCK_KEY error");
                }
            }
        }

        return "商品已经售罄/活动结束/调用超时，欢迎下次光临" + "\t 服务器端口：" + serverPort;
    }

    @Autowired
    private Redisson redisson;

    @GetMapping("/buy_goods_redisson")
    public String buy_Goods2() {

        RLock lock = redisson.getLock(KEY);
        lock.lock();

        try {
            // 业务代码
            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);

            if (goodsNumber > 0) {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001", realNumber + "");
                System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件" + "\t 服务器端口：" + serverPort);
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件" + "\t 服务器端口：" + serverPort;
            } else {
                System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临" + "\t 服务器端口：" + serverPort);
            }
        } finally {
            // 问题：避免解锁太快，把别人给解锁了
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return "商品已经售罄/活动结束/调用超时，欢迎下次光临" + "\t 服务器端口：" + serverPort;
    }
}

