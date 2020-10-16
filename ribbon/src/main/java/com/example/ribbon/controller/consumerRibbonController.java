package com.example.ribbon.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @CLASSNAME consumerRibbonController
 * @PROJECTNAME com.example.ribbon.controller
 * @Description
 * @Author 鲁少博 on 2020/10/12  14:20
 * @Version V1.0
 */
@RestController
public class consumerRibbonController {

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/listUsersByRibbon")
    @HystrixCommand(fallbackMethod = "listUsersByRibbonFallback")
    public String listUsersByRibbon(){
        final String forObject = this.restTemplate.getForObject("http://consumer/listUsers", String.class);
        return forObject;
    }

    @GetMapping("/getUsersByRibbon")
    @HystrixCommand(fallbackMethod = "getUsersByRibbonFallback")
    public String getUsersByRibbon(){
        final String forObject = this.restTemplate.getForObject("http://consumer/getUser", String.class);
        return forObject;
    }

    public String listUsersByRibbonFallback(){
        return "this is fallback for ribbon list user";
    }

    public String getUsersByRibbonFallback(){
        return "this is fallback for ribbon get user";
    }

}
