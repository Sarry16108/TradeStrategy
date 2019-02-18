package com.example.finance.tradestrategy.entity;

import com.example.finance.tradestrategy.utils.ToolGson;
import com.google.gson.JsonObject;

/**
 * Created by Administrator on 2017/6/1.
 */
public abstract class BaseResponse{

    /**
     * success : true
     * message : 获取成功
     */

    private int ret = 1;        //0:成功，30001：失败，无值：url类错误。
    //url类错误
    private String error;
    private String error_description;

    //请求处理结果错误
    private String msgCn;
    private String msg;

    public boolean isRet() {
        return 0 == ret;
    }

    public String getError() {
        if (30001 == ret) {
            return msgCn + "  description:" + msg;
        } else {
            return error + "  description:" + error_description;
        }
    }

}
