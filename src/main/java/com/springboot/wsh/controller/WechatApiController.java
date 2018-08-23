package com.springboot.wsh.controller;

import com.springboot.wsh.service.WechatApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLDecoder;

@Controller
@RequestMapping("/wechat/api")
public class WechatApiController {

    @Autowired
    private WechatApiService wechatApiService;

    @GetMapping(value = "/getWeiXinJsConfig")
    @ResponseBody
    public Object getWeiXinJsConfig(String signUrl) throws Exception {
        signUrl = URLDecoder.decode(signUrl, "UTF-8");
        return wechatApiService.getWeiXinJsConfig(signUrl);
    }

}
