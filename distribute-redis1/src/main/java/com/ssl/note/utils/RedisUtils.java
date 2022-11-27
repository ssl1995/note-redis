package com.ssl.note.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author SongShengLin
 * @date 2022/11/27 18:25
 * @description
 */
public class RedisUtils {

    private static final JedisPool jedisPool;

    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        String host = "101.201.154.144";
        String password = "Ssl@134679";
        jedisPool = new JedisPool(jedisPoolConfig, host, 6379, 100 * 1000, password, 1);
    }

    public static Jedis getJedis() throws Exception {
        if (null != jedisPool) {
            return jedisPool.getResource();
        }
        throw new Exception("Jedispool was not init");
    }
}
