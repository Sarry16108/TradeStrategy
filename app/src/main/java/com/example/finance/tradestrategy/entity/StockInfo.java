package com.example.finance.tradestrategy.entity;

import java.util.List;

/**
 * Created by Administrator on 2017/7/12.
 * 每次请求返回的股票数据
 */

public class StockInfo extends BaseResponse{
    /**
     * ret : 0
     * detail : {"askSize":3,"change":2,"halted":0,"latestTime":"07-12 05:50:56 EDT","bidSize":10,"latestPrice":145.81,"preClose":143.81,"nameCN":"阿里巴巴","timestamp":1499852912677,"bidPrice":146.5,"tradingStatus":1,"amount":1.958710770912E9,"open":144.29,"volume":13519912,"high":145.87,"hourTrading":{"tag":"盘前","latestPrice":146.88,"preClose":145.81,"latestTime":"05:48 EDT","volume":4799,"timestamp":1499852912677},"low":143.26,"marketStatus":"盘前交易","exchange":"NYSE","askPrice":146.9,"amplitude":0.018149}
     * symbol : BABA
     * items : [{"open":144.1,"time":1499788080000,"volume":6348,"high":144.17,"low":144.08,"close":144.17},{"open":144.18,"time":1499788140000,"volume":9585,"high":144.2017,"low":144.08,"close":144.13}]
     * serverTime : 1499853056946
     * period : 1min
     */

    private DetailInfo detail;
    private String symbol;
    private long serverTime;
    private String period;          //指定是1分还是5
    private List<TradeInfo> items;  //历史数据或者指定分钟内的数据
    private List<TradeInfo> items5;
    private List<TradeInfo> items15;
    private List<TradeInfo> items30;
    private List<TradeInfo> items60;

    public DetailInfo getDetail() {
        return detail;
    }

    public void setDetail(DetailInfo detail) {
        this.detail = detail;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    //1min
    public List<TradeInfo> getItems() {
        return items;
    }

    public void setItems(List<TradeInfo> items) {
        this.items = items;
    }

    public void addItemsHead(List<TradeInfo> items) {
        this.items.addAll(0, items);
    }

    //5min
    public List<TradeInfo> getItems5() {
        return items5;
    }

    public void setItems5(List<TradeInfo> items5) {
        this.items5 = items5;
    }

    public void addItems5Head(List<TradeInfo> items5) {
        this.items5.addAll(0, items5);
    }

    //15min
    public List<TradeInfo> getItems15() {
        return items15;
    }

    public void setItems15(List<TradeInfo> items15) {
        this.items15 = items15;
    }
    public void addItems15Head(List<TradeInfo> items15) {
        this.items15.addAll(0, items15);
    }

    //30min
    public List<TradeInfo> getItems30() {
        return items30;
    }

    public void setItems30(List<TradeInfo> items30) {
        this.items30 = items30;
    }
    public void addItems30Head(List<TradeInfo> items30) {
        this.items30.addAll(0, items30);
    }


    //60min
    public List<TradeInfo> getItems60() {
        return items60;
    }

    public void setItems60(List<TradeInfo> items60) {
        this.items60 = items60;
    }
    public void addItems60Head(List<TradeInfo> items60) {
        this.items60.addAll(0, items60);
    }

    public static class DetailInfo {
        /**
         * askSize : 3
         * change : 2
         * halted : 0
         * latestTime : 07-12 05:50:56 EDT
         * bidSize : 10
         * latestPrice : 145.81
         * preClose : 143.81
         * nameCN : 阿里巴巴
         * timestamp : 1499852912677
         * bidPrice : 146.5
         * tradingStatus : 1
         * amount : 1.958710770912E9
         * open : 144.29
         * volume : 13519912
         * high : 145.87
         * hourTrading : {"tag":"盘前","latestPrice":146.88,"preClose":145.81,"latestTime":"05:48 EDT","volume":4799,"timestamp":1499852912677}
         * low : 143.26
         * marketStatus : 盘前交易
         * exchange : NYSE
         * askPrice : 146.9
         * amplitude : 0.018149
         */

        private int askSize;        //卖数
        private float change;         //开盘价格浮动值
        private float halted;         //
        private String latestTime;      //最新时间
        private int bidSize;            //买数
        private float latestPrice;     //最新价
        private float preClose;        //前一日收盘
        private String nameCN;          //名字
        private long timestamp;         //最新时间
        private float bidPrice;        //买价
        private int tradingStatus;
        private float amount;
        private float open;                    //当天开盘价
        private int volume;                     //当天交易量
        private float high;                    //当天最高
        private HourTradingBean hourTrading;
        private float low;                     //当天最低
        private String marketStatus;
        private String exchange;                //交易市场
        private float askPrice;                //卖价
        private float amplitude;               //涨跌幅度

        public int getAskSize() {
            return askSize;
        }

        public void setAskSize(int askSize) {
            this.askSize = askSize;
        }

        public float getChange() {
            return change;
        }

        public void setChange(int change) {
            this.change = change;
        }

        public float getHalted() {
            return halted;
        }

        public void setHalted(int halted) {
            this.halted = halted;
        }

        public String getLatestTime() {
            return latestTime;
        }

        public void setLatestTime(String latestTime) {
            this.latestTime = latestTime;
        }

        public int getBidSize() {
            return bidSize;
        }

        public void setBidSize(int bidSize) {
            this.bidSize = bidSize;
        }

        public float getLatestPrice() {
            return latestPrice;
        }

        public void setLatestPrice(float latestPrice) {
            this.latestPrice = latestPrice;
        }

        public float getPreClose() {
            return preClose;
        }

        public void setPreClose(float preClose) {
            this.preClose = preClose;
        }

        public String getNameCN() {
            return nameCN;
        }

        public void setNameCN(String nameCN) {
            this.nameCN = nameCN;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public float getBidPrice() {
            return bidPrice;
        }

        public void setBidPrice(float bidPrice) {
            this.bidPrice = bidPrice;
        }

        public int getTradingStatus() {
            return tradingStatus;
        }

        public void setTradingStatus(int tradingStatus) {
            this.tradingStatus = tradingStatus;
        }

        public float getAmount() {
            return amount;
        }

        public void setAmount(float amount) {
            this.amount = amount;
        }

        public float getOpen() {
            return open;
        }

        public void setOpen(float open) {
            this.open = open;
        }

        public int getVolume() {
            return volume;
        }

        public void setVolume(int volume) {
            this.volume = volume;
        }

        public float getHigh() {
            return high;
        }

        public void setHigh(float high) {
            this.high = high;
        }

        public HourTradingBean getHourTrading() {
            return hourTrading;
        }

        public void setHourTrading(HourTradingBean hourTrading) {
            this.hourTrading = hourTrading;
        }

        public float getLow() {
            return low;
        }

        public void setLow(float low) {
            this.low = low;
        }

        public String getMarketStatus() {
            return marketStatus;
        }

        public void setMarketStatus(String marketStatus) {
            this.marketStatus = marketStatus;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public float getAskPrice() {
            return askPrice;
        }

        public void setAskPrice(float askPrice) {
            this.askPrice = askPrice;
        }

        public float getAmplitude() {
            return amplitude;
        }

        public void setAmplitude(float amplitude) {
            this.amplitude = amplitude;
        }

        public static class HourTradingBean {
            /**
             * tag : 盘前
             * latestPrice : 146.88
             * preClose : 145.81
             * latestTime : 05:48 EDT
             * volume : 4799
             * timestamp : 1499852912677
             */

            private String tag;
            private float latestPrice;
            private float preClose;
            private String latestTime;
            private int volume;
            private long timestamp;

            public String getTag() {
                return tag;
            }

            public void setTag(String tag) {
                this.tag = tag;
            }

            public float getLatestPrice() {
                return latestPrice;
            }

            public void setLatestPrice(float latestPrice) {
                this.latestPrice = latestPrice;
            }

            public float getPreClose() {
                return preClose;
            }

            public void setPreClose(float preClose) {
                this.preClose = preClose;
            }

            public String getLatestTime() {
                return latestTime;
            }

            public void setLatestTime(String latestTime) {
                this.latestTime = latestTime;
            }

            public int getVolume() {
                return volume;
            }

            public void setVolume(int volume) {
                this.volume = volume;
            }

            public long getTimestamp() {
                return timestamp;
            }

            public void setTimestamp(long timestamp) {
                this.timestamp = timestamp;
            }
        }
    }
}
