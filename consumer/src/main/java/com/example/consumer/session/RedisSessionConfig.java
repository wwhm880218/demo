package com.example.consumer.session;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @CLASSNAME RedisSessionConfig
 * @PROJECTNAME com.example.consumer.session
 * @Description
 * @Author 鲁少博 on 2020/10/15  9:36
 * @Version V1.0
 */
@EnableRedisHttpSession
@Configuration
public class RedisSessionConfig {
}
