package com.example.finance.tradestrategy.globaldata;

/**
 * Created by Administrator on 2017/6/1.
 */

public interface InitData {

    //排行榜类型
    String RankTypeRevenue  = "REVENUE";     //收益
    String RankTypeWinRatio = "WINRATIO";   //胜率

    //时间范围
    String TimeTypeDate    = "DATE";     //按日
    String TimeTypeWeek    = "WEEK";     //按周
    String TimeTypeMonth   = "MONTH";   //按月
    String TimeTypeWhole   = "WHOLE";    //30天

    //信息值：containFut
    String ContainAll = "ALL";   //所有


    String RecordStatusDraft  = "DRAFT";    //持有
    String RecordStatusFinish  = "FINISH";    //卖出

    //多空
    String BuyTypeBull = "BULL";    //多
    String BuyTypeBear = "BEAR";    //空



    //sharedPreferences name
    String  TigerStockCodes     = "tigerStockCodes";    //监控的股票代码
    String  SpKeyUserLoginInfo = "userLoginInfo";       //用户账号信息



    //页面跳转标记
    String  ACT_MARK_MAIN_ACT   =   "act_main";

    //股票代码
    public String Stock_UVXY = "UVXY";
    public String Stock_AMD = "AMD";
    public String Stock_JD = "JD";
    public String Stock_BABA = "BABA";
    public String Stock_X = "X";
    public String Stock_NVDA = "NVDA";
    public String Stock_UWT = "UWT";
    public String Stock_DWT = "DWT";
    public String Stock_MOMO = "MOMO";
    public String Stock_JDST = "JDST";
    public String Stock_DGAZ = "DGAZ";
    public String Stock_UGAZ = "UGAZ";
    public String Stock_JNUG = "JNUG";
    public String Stock_NUGT = "NUGT";
    public String Stock_DUST = "DUST";
}
