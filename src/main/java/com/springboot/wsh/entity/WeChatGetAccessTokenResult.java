package com.springboot.wsh.entity;

public class WeChatGetAccessTokenResult extends WeChatBaseResult {

    /**
     * token有效时间 暂时为2700秒
     */
    private Integer expires_in;
    /**
     * access_token
     */
    private String access_token;

    public Integer getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Integer expires_in) {
        this.expires_in = expires_in;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
