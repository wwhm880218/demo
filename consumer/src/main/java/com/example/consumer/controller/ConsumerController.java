package com.example.consumer.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CLASSNAME ConsumerController
 * @PROJECTNAME com.example.consumer.controller
 * @Description
 * @Author 鲁少博 on 2020/10/10  15:58
 * @Version V1.0
 */
@RestController
@RefreshScope
public class ConsumerController {
    @Value("${server.port}")
    String serverPort;

    @Value("${server.time}")
    String serverTime;
    @GetMapping("/listUsers")
    public String getUsers(){
        List<Map<String,Object>> users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String,Object> user = new HashMap<>();
            user.put("id",i);
            user.put("name","张三"+i);
            users.add(user);
        }
        return "端口号 : "+serverPort+"----- 用户信息 : " + users.toString()+"------ 服务时间 : "+serverTime;


    }
    @GetMapping("/getUser")
    public String getUser(HttpServletRequest httpServletRequest){
        final HttpSession session = httpServletRequest.getSession();
        final String username = (String)session.getAttribute("username");
        if (StringUtils.isEmpty(username)){
            session.setAttribute("username","testSessionRedis"+System.currentTimeMillis()+" - "+serverPort);
        }
        return username+"-"+serverPort;
    }
}
