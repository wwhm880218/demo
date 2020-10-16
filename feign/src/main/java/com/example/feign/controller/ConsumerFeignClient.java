package com.example.feign.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @CLASSNAME ConsumerFeignClient
 * @PROJECTNAME com.example.feign.controller
 * @Description
 * @Author 鲁少博 on 2020/10/12  14:57
 * @Version V1.0
 */
@FeignClient(name = "consumer",fallback = ConsumerFeignHystrixClient.class)
public interface ConsumerFeignClient {

    @GetMapping("/listUsers")
    public String listUsers();

    @GetMapping("/getUser")
    public String getUser();
}
