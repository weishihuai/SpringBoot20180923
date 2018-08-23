package com.springboot.wsh.service;

import com.springboot.wsh.entity.WeChatGetAccessTokenResult;
import com.springboot.wsh.entity.WeiXinJsSdkConfigVo;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface WechatApiService {

    WeiXinJsSdkConfigVo getWeiXinJsConfig(String signUrl) throws Exception;

    String getToken();

    String getAccessToken() throws Exception;

    /**
     * 刷新token
     */
    void refreshGetAccessToken() throws IOException;

    /**
     * 重置token过期时间
     */
    void doWeChatAccessTokenTimeReset();

    /**
     * 获取 JsApi_ticket
     */
    String getJsApiTicket() throws Exception;

    void doWeChatAccessTokenTimePlus();

    WeChatGetAccessTokenResult getWeChatGetAccessTokenResult();

    Integer getWeChatAccessTokenTime();

    void doWeChatGetAccessTokenResultReset();
}
