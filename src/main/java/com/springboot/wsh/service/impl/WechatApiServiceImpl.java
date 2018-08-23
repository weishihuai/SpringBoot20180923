package com.springboot.wsh.service.impl;

import com.alibaba.fastjson.JSON;
import com.springboot.wsh.entity.WeChatBaseResult;
import com.springboot.wsh.entity.WeChatGetAccessTokenResult;
import com.springboot.wsh.entity.WeiXinGetJsApiTicketResult;
import com.springboot.wsh.entity.WeiXinJsSdkConfigVo;
import com.springboot.wsh.service.WechatApiService;
import com.springboot.wsh.utils.WeChatWebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WechatApiServiceImpl implements WechatApiService {

    private static final String UTF_8 = "UTF-8";
    private static final String GRANT_TYPE = "grant_type";
    private static final String APP_ID = "appid";
    private static final String APP_SECRET = "secret";
    private static int weChatAccessTokenTime = 0; //秒
    private static WeChatGetAccessTokenResult weChatGetAccessTokenResult = null;
    private static WeiXinGetJsApiTicketResult weiXinGetJsApiTicketResult = null;
    private static Long weixinGetJsapiTicketEffectiveTime = 0L;  //JsapiTicket 有效时间，毫秒
    private String token;


    @Value("${app.init.weixinconfig.service.token}")
    public void setToken(String token) {
        this.token = token;
    }

    //具体项目中需要存在系统参数或者其他方式存储起来
    @Value("${app.init.weixinconfig.service.appId}")
    private String initAppId;
    @Value("${app.init.weixinconfig.service.appSecret}")
    private String initAppSecret;

    @Override
    public WeiXinJsSdkConfigVo getWeiXinJsConfig(String signUrl) throws Exception {
        String jsApiTicket = this.getJsApiTicket();

        WeiXinJsSdkConfigVo weiXinJsSdkConfigVo = new WeiXinJsSdkConfigVo();
        //实际项目中需要从系统参数中获取或者其他方式存储起来
        //appId
        weiXinJsSdkConfigVo.setAppId(initAppId);
        //随机字符串
        weiXinJsSdkConfigVo.setNonceStr(create_nonce_str());
        //时间戳
        weiXinJsSdkConfigVo.setTimestamp(create_timestamp());

        //生成签名signature  需要的参数: 随机字符串  时间戳  appId  jsApiTicket
        StringBuffer signature = new StringBuffer("");
        signature.append("jsapi_ticket=");
        signature.append(jsApiTicket);
        signature.append("&noncestr=");
        signature.append(weiXinJsSdkConfigVo.getNonceStr());
        signature.append("&timestamp=");
        signature.append(weiXinJsSdkConfigVo.getTimestamp());
        signature.append("&url=");
        signature.append(signUrl);

        //java提供的加密类 使用SHA-1进行加密
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.reset();
        messageDigest.update(signature.toString().getBytes("UTF-8"));

        //签名
        weiXinJsSdkConfigVo.setSignature(byteToHex(messageDigest.digest()));

        return weiXinJsSdkConfigVo;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getAccessToken() throws Exception {
        if (null == weChatGetAccessTokenResult || StringUtils.isBlank(weChatGetAccessTokenResult.getAccess_token())) {
            //重新刷新access_token
            refreshGetAccessToken();
        }
        if (null == weChatGetAccessTokenResult || StringUtils.isBlank(weChatGetAccessTokenResult.getAccess_token())) {
            throw new Exception("获取Access_Token失败!");
        }
        return weChatGetAccessTokenResult.getAccess_token();
    }



    @Override
    public void refreshGetAccessToken() throws IOException {
        //具体项目中需要从其他地方获取（数据库或者其他）
        String appId = initAppId;
        String appSecret = initAppSecret;

        Map<String, String> params = new HashMap<>();
        params.put(GRANT_TYPE, "client_credential");
        params.put(APP_ID, appId);
        params.put(APP_SECRET, appSecret);

        //请求地址
        //https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wxebce2fd0fe02ac31&secret=c84c5187a176b2543f5c0ab8488d92b1

        //get请求获取Access_Token
        String response = WeChatWebUtil.doGet("https://api.weixin.qq.com/cgi-bin/token", params, UTF_8, 3000, 3000);

        weChatGetAccessTokenResult = JSON.parseObject(response, WeChatGetAccessTokenResult.class);
        if (null != weChatGetAccessTokenResult && StringUtils.isBlank(weChatGetAccessTokenResult.getAccess_token())) {
            weChatGetAccessTokenResult = null;
        }

        //重置token过期时间
        doWeChatAccessTokenTimeReset();
    }

    @Override
    public void doWeChatAccessTokenTimeReset() {
        weChatAccessTokenTime = 0;
    }

    @Override
    public String getJsApiTicket() throws Exception {
        //检查js_ticket是否有效
        if (checkJsapiTicketEffective()) {
            //有效,直接返回jsApiTicket
            return weiXinGetJsApiTicketResult.getTicket();
        } else {
            //无效，需要根据access_token去重新获取
            Map<String, String> param = new HashMap<>();
            param.put("access_token", getAccessToken().trim());
            param.put("type", "jsapi");
            String responseJson = WeChatWebUtil.doGet("https://api.weixin.qq.com/cgi-bin/ticket/getticket", param, UTF_8, 3000, 3000);
            weiXinGetJsApiTicketResult = JSON.parseObject(responseJson, WeiXinGetJsApiTicketResult.class);
            //检查处理请求返回结果
            checkResultError(weiXinGetJsApiTicketResult);

            //更新jsApiTicket最后失效时间  (当前时间 +　接口返回的过期时长)
            weixinGetJsapiTicketEffectiveTime = System.currentTimeMillis() + weiXinGetJsApiTicketResult.getExpires_in() * 1000;
            return weiXinGetJsApiTicketResult.getTicket();
        }
    }

    /**
     * 检查处理请求返回结果
     *
     */
    private void checkResultError(WeChatBaseResult weChatBaseResult)  {
        if (weChatBaseResult == null || weChatBaseResult.getErrCode() == null || weChatBaseResult.getErrCode() == 0) {
            return;
        }
        Integer errorCode = weChatBaseResult.getErrCode();
        //40001 42001是token问题导致，所以需要刷新token
        if (errorCode == 40001 || errorCode == 42001) {
            //清空access_token，重新获取access_token
            this.doWeChatGetAccessTokenResultReset();
        }
    }

    @Override
    public void doWeChatGetAccessTokenResultReset() {
        weChatGetAccessTokenResult = null;
    }


    @Override
    public void doWeChatAccessTokenTimePlus() {
        weChatAccessTokenTime++;
    }

    @Override
    public WeChatGetAccessTokenResult getWeChatGetAccessTokenResult() {
        return weChatGetAccessTokenResult;
    }

    /**
     * 检查js_ticket是否有效
     */
    private boolean checkJsapiTicketEffective() {
        long currentTime = System.currentTimeMillis();
        //jsApiTicket为空 或 当前时间 > jsApiTicket的最后失效时间
        return !(null == weiXinGetJsApiTicketResult || StringUtils.isBlank(weiXinGetJsApiTicketResult.getTicket()) || currentTime > weixinGetJsapiTicketEffectiveTime);
    }

    @Override
    public Integer getWeChatAccessTokenTime() {
        return weChatAccessTokenTime;
    }

}
