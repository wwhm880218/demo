package com.example.eureka;

import com.example.eureka.tt.HWCE6851SSHClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
        HWCE6851SSHClient sshClient = new HWCE6851SSHClient();
        sshClient.setHost("10.150.66.32");
        sshClient.setPort(22);
        sshClient.setUsername("root");
        sshClient.setPassword("%k4tnew6F0E38YXR");
        sshClient.login();
        List<String> commands = new ArrayList<>();
        commands.add("ip addr");
        final String dis_cur = sshClient.sendCmdShell(commands);
        System.out.println(dis_cur+"bbbbbbbbbbbbb");
    }

}
