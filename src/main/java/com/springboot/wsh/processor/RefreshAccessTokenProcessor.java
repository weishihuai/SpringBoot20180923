package com.springboot.wsh.processor;

import com.springboot.wsh.service.WechatApiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 刷新access_token定时器
 */
@Component
public class RefreshAccessTokenProcessor {

    @Autowired
    private WechatApiService wechatApiService;

    /**
     * 定时刷新access_token
     */
    @Scheduled(cron = "${app.processor.weiXinRefreshGetAccessToken.frequency:0/1 * * * * ?}")
    public void refreshAccessTokenProcess() throws IOException {
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println("当前时间为:" + localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        //更新access_token过期时间
        wechatApiService.doWeChatAccessTokenTimePlus();
        if (null == wechatApiService.getWeChatGetAccessTokenResult() || StringUtils.isBlank(wechatApiService.getWeChatGetAccessTokenResult().getAccess_token()) || wechatApiService.getWeChatAccessTokenTime() + 300 > wechatApiService.getWeChatGetAccessTokenResult().getExpires_in()) {
            wechatApiService.refreshGetAccessToken();
        }
    }


}
