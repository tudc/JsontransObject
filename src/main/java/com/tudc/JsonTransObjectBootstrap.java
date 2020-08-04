package com.tudc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author
 * @create 2020-07-28 15:55
 * @contact
 **/
@SpringBootApplication
public class JsonTransObjectBootstrap {

    public static void main(String[] args) {
        System.out.println("这是dev分支");
        SpringApplication.run(JsonTransObjectBootstrap.class,args);
    }
}
