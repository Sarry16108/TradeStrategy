package com.example.finance.tradestrategy.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/13.
 */

public class StockStrategy {
    private int     pos = 0;    //列表中位置
    private String  nameCN;
    private String  symbol;
    private float   close;

    //策略组，只要size不为0，则代表有购买的策略
    private List<Integer> buyBullStrategies;
    private List<Integer> buyBearStrategies;

    //策略组，只要size不为0，则代表有卖出的策略
    private List<Integer> sellBullStrategies;
    private List<Integer> sellBearStrategies;
/*
    //单项的预测
    public static class Strategy {
        private boolean buy;        //该方向上的预测，InitAppConstant.FORECAST_LEVEL_NEUTRAL
        private int     strategyType = InitAppConstant.FORECAST_BULL_BEAR;    //

    }*/

    public StockStrategy(String nameCN, String symbol, int pos) {
        this.nameCN = nameCN;
        this.symbol = symbol;
        this.pos = pos;
        buyBullStrategies = new ArrayList<>(2);
        buyBearStrategies = new ArrayList(2);
        sellBullStrategies = new ArrayList<>(2);
        sellBearStrategies = new ArrayList<>(2);
    }

    public int getPos() {
        return pos;
    }

    public String getNameCN() {
        return nameCN;
    }

    public void setNameCN(String nameCN) {
        this.nameCN = nameCN;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setClose(float close) {
        this.close = close;
    }

    public float getClose() {
        return close;
    }

    public void addBuyBullStrategy(int strategy) {
        buyBullStrategies.add(strategy);
    }

    public void addBuyBearStrategy(int strategy) {
        buyBearStrategies.add(strategy);
    }

    public void addSellBullStrategy(int strategy) {
        sellBullStrategies.add(strategy);
    }

    public void addSellBearStrategy(int strategy) {
        sellBearStrategies.add(strategy);
    }

    /**
     * 是否购买
     */
    public boolean isBuy() {
        return buyBullStrategies.size() > 0 || buyBearStrategies.size() > 0;
    }

    public void resetBuy() {
        buyBullStrategies.clear();
        buyBearStrategies.clear();
    }

    public List<Integer> getBuyBullStrategies() {
        return buyBullStrategies;
    }

    public List<Integer> getBuyBearStrategies() {
        return buyBearStrategies;
    }

    /**
     * 是否卖出
     */
    public boolean isSell() {
        return sellBullStrategies.size() > 0 || sellBearStrategies.size() > 0;
    }

    public void resetSell() {
        sellBullStrategies.clear();
        sellBearStrategies.clear();
    }

    public List<Integer> getSellBullStrategies() {
        return sellBullStrategies;
    }


    public List<Integer> getSellBearStrategies() {
        return sellBearStrategies;
    }
}
