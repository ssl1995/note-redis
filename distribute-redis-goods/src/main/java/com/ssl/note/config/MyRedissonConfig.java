package com.ssl.note.config;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author SongShengLin
 * @date 2022/11/27 10:35
 * @description
 */
@Configuration
public class MyRedissonConfig {
    @Value("${redis.host}")
    private String host;

    @Value("${redis.password}")
    private String password;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.timeout}")
    private int timeout;

    @Value("${redis.maxIdle}")
    private int maxIdle;

    @Value("${redis.maxWaitMillis}")
    private int maxWaitMillis;

    @Value("${redis.blockWhenExhausted}")
    private Boolean blockWhenExhausted;

    @Value("${redis.JmxEnabled}")
    private Boolean JmxEnabled;


    @Bean(destroyMethod = "shutdown")
    public Redisson redisson() {
        Config config = new Config();

        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password)
                .setDatabase(2)
        ;

        return (Redisson) Redisson.create(config);
    }


}
