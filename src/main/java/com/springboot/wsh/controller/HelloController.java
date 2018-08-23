package com.springboot.wsh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

//如果
@Controller
public class HelloController {

//    @RequestMapping("/hello")
//    public String hello() {
//        return "Hello Spring Boot!";
//    }

    @RequestMapping("/index")
    public String index() {
        return "index";
    }

}
