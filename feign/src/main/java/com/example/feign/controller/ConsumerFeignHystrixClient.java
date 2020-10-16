package com.example.feign.controller;

import org.springframework.stereotype.Component;

/**
 * @CLASSNAME ConsumerFeignHystrixClient
 * @PROJECTNAME com.example.feign.controller
 * @Description
 * @Author 鲁少博 on 2020/10/12  15:10
 * @Version V1.0
 */
@Component
public class ConsumerFeignHystrixClient implements ConsumerFeignClient {
    @Override
    public String listUsers() {
        return "this is fallback for feign";
    }

    @Override
    public String getUser() {
        return "this is fallback for feign get user";
    }
}
