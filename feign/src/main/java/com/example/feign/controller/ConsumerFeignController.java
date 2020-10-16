package com.example.feign.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @CLASSNAME ConsumerFeignController
 * @PROJECTNAME com.example.feign.controller
 * @Description
 * @Author 鲁少博 on 2020/10/12  14:56
 * @Version V1.0
 */

@RestController
public class ConsumerFeignController {

    @Autowired
    private ConsumerFeignClient consumerFeignClient;


    @GetMapping("/listUsersByFeign")
    public String listUsersByFeign(){
        final String s = this.consumerFeignClient.listUsers();
        return s;
    }

    @GetMapping("/getUserByFeign")
    public String getUserByFeign(){
        final String s = this.consumerFeignClient.getUser();
        return s;
    }

}
