package com.ssl.note.config;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author SongShengLin
 * @date 2022/11/27 18:44
 * @description
 */
@Configuration
public class RedissonConfig {

    @Bean
    public Redisson redisson() {
        Config config = new Config();

        config.useSingleServer().setAddress("redis://101.201.154.144:6379").setPassword("Ssl@134679").setDatabase(1);

        return (Redisson) Redisson.create(config);
    }
}
