package com.example.finance.tradestrategy.entity;

import android.text.TextUtils;

/**
 * Created by Administrator on 2017/7/13.
 * authorization、cookie先关
 */

public class QueryAuth {
    private String ngxid="";
    private String sessionId="";
    private String authorization=" ";//"Bearer 9BgeOWNftoAyuKDAHMLo1lxwO1x4oQeRZHlNgcb58NgLgDuEIR";

    public String getNgxid() {
        return ngxid;
    }

    public void setNgxid(String ngxid) {
        this.ngxid = ngxid;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getCookie() {
        if (TextUtils.isEmpty(ngxid) || TextUtils.isEmpty(sessionId)) {
            return "";
        } else {
            return "ngxid=" + ngxid + ";JSESSIONID=" + sessionId;
        }
    }
}
