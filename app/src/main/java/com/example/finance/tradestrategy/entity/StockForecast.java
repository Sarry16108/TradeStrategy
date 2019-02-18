package com.example.finance.tradestrategy.entity;

/**
 * Created by Administrator on 2017/7/17.
 */

public class StockForecast {
    private String  nameCN;
    private String  symbol;
    private float   price;
    private long    createTime;     //创建时间
    private String  forecastDetail;     //预测的详情

    public StockForecast(String nameCN, String symbol, float price, String forecastDetail, long createTime) {
        this.nameCN = nameCN;
        this.symbol = symbol;
        this.createTime = createTime;
        this.forecastDetail = forecastDetail;
        this.price = price;
    }

    public String getNameCN() {
        return nameCN;
    }

    public String getSymbol() {
        return symbol;
    }

    public float getPrice() {
        return price;
    }

    public String getForecastDetail() {
        return forecastDetail;
    }

    public long getCreateTime() {
        return createTime;
    }
}
