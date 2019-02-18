package com.example.finance.tradestrategy.indicators.calculate;

import android.support.v4.util.ArrayMap;

import com.example.finance.tradestrategy.entity.AnalyzeTmpData;
import com.example.finance.tradestrategy.entity.StockInfo;
import com.example.finance.tradestrategy.entity.StockStrategy;
import com.example.finance.tradestrategy.entity.TradeInfo;
import com.example.finance.tradestrategy.globaldata.InitAppConstant;
import com.example.finance.tradestrategy.globaldata.InitData;
import com.example.finance.tradestrategy.utils.ToolData;
import com.example.finance.tradestrategy.utils.ToolLog;
import com.example.finance.tradestrategy.utils.ToolMath;
import com.example.finance.tradestrategy.utils.ToolTime;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/13.
 */

public enum AnalyzeInd {
    INSTANCE;
    private final String TAG = "AnalyzeInd";

    //为了减少每次获得数据后的遍历计算，保存一些中间值，放在这里是为了减少map.get()及map.containKey()判断
    private Map<String, AnalyzeTmpData> mAnalyTmpData = new ArrayMap<>(10);
    //记录当前分析的结果，但是由于不利于以后的扩展，所以，采用每次调用checkResult遍历的方式
//    private Map<String, AnalyzeResult>  mAnalyzeResult = new ArrayMap<>(10);
    //股票开市的时间区间，对应着相应的策略起作用
    private long mFirstPeriodStart = 0, mFirstPeriodEnd = 0;

    /**
     *
     * @param type          分钟类型
     * @param historyInfo   历史记录数据
     * @param stockInfo     最新获取更新数据
     * @param period        当前请求的时间段
     * @param stockStrategy
     * @return
     */
    public synchronized long analyzeStockInfoResult(int type, StockInfo historyInfo, StockInfo stockInfo, Long period, StockStrategy stockStrategy) {
        //分钟边界处会存在两条记录，第一条是上分钟数据，第二条是当前分钟前几秒数据
        int count = stockInfo.getItems().size();
        if (0 == count) {
            return period;
        }

        //判断历史记录中最后一条数据和当前最新数据
        List<TradeInfo> historyTrades = null;

        //如果等于最后一条，同一个时间段，则更新，否则添加历史记录末尾。
        //如果是新的时间段，则将tmpData修改，否则，都是在上一时间段基础上进行修改且不保存。
        AnalyzeTmpData analyzeTmpData = mAnalyTmpData.get(stockInfo.getSymbol());
        AnalyzeTmpData.TmpData tmpData = null;

        switch (type) {
            case InitAppConstant.MINUTE_1:
                historyTrades = historyInfo.getItems();
                tmpData = analyzeTmpData.getM1();
                break;
            case InitAppConstant.MINUTE_5:
                historyTrades = historyInfo.getItems5();
                tmpData = analyzeTmpData.getM5();
                break;
            case InitAppConstant.MINUTE_15:
                historyTrades = historyInfo.getItems15();
                tmpData = analyzeTmpData.getM15();
                break;
            case InitAppConstant.MINUTE_30:
                historyTrades = historyInfo.getItems30();
                tmpData = analyzeTmpData.getM30();
                break;
            case InitAppConstant.MINUTE_60:
                historyTrades = historyInfo.getItems60();
                tmpData = analyzeTmpData.getM60();
                break;
            default:
                break;
        }
        if (null == historyTrades) {
            return period;
        }

        int lastIndex = historyTrades.size() - 1;
        TradeInfo hisLastInfo = historyTrades.get(lastIndex);
        TradeInfo tradeUpdate = stockInfo.getItems().get(0);

        if (tradeUpdate.getTime() == hisLastInfo.getTime()) {
            historyTrades.set(lastIndex, tradeUpdate);

            //如果有两条数据，代表着新的一分钟数据记录开始，这时，tmpData就不再new新的，目的是将此次计算的改变值应用到下一次。
            if (2 == count) {
                period = stockInfo.getItems().get(1).getTime();
            } else {
                tmpData = new AnalyzeTmpData.TmpData(tmpData);
            }
        } else {
            historyTrades.add(stockInfo.getItems().get(0));
            tmpData = new AnalyzeTmpData.TmpData(tmpData);
        }

        //各种指标计算，因为计算耗时，最好是一次更新执行一次。
        analyzeNewIndex(stockInfo.getSymbol(), historyTrades, tmpData);
        stockStrategy.setClose(tradeUpdate.getClose());
        checkResult(type,stockInfo.getServerTime(),  historyInfo, stockStrategy);

        dataCompaction(historyInfo);
        return period;
    }

    /**
     * 缩减数据，当数据量达到上限值时候，删除到下限值。
     * @param historyInfo
     */
    private void dataCompaction(StockInfo historyInfo) {
        ToolData.dataCompactionFromHead(historyInfo.getItems(), 200, 120);
        ToolData.dataCompactionFromHead(historyInfo.getItems5(), 200, 120);
        ToolData.dataCompactionFromHead(historyInfo.getItems15(), 200, 120);
    }


    /**
     * 所有的指标联合分析，都在这里进行，然后保存入stockStrategy中进行显示。
     * @param type
     * @param stockStrategy
     * @param , prevTradeInfo , tradeInfo
     */
    private void checkResult(int type, long serverTime, StockInfo historyInfo, StockStrategy stockStrategy) {
        //清空策略信息
        stockStrategy.resetBuy();
        stockStrategy.resetSell();


        if (InitAppConstant.MINUTE_5 == type) {
            List<TradeInfo> tradeInfos5 = historyInfo.getItems5();
            int sizeM5 = tradeInfos5.size();

            TradeInfo tradeInfoM5 = tradeInfos5.get(sizeM5 - 1), preTradeInfoM5 = tradeInfos5.get(sizeM5 - 2), preThirdTradeInfoM5 = tradeInfos5.get(sizeM5 - 3),
                    preFourTradeInfoM5 = tradeInfos5.get(sizeM5 - 3);

            switch (historyInfo.getSymbol()) {
                case InitData.Stock_UVXY:
                    strategyMacdDiffOverturn3(stockStrategy, tradeInfoM5, preTradeInfoM5, preThirdTradeInfoM5, preFourTradeInfoM5);
                    strategyMacdDiffOverturn5(stockStrategy, tradeInfoM5, preTradeInfoM5, preThirdTradeInfoM5, preFourTradeInfoM5);
                    break;
            }
        }
    }


    /**
     * macd出现拐点
     * 空：开口向下，preMacd>0.06 && preThirdMacd.getMacd() > 0.1，diff都在dea上
     * 多：开口向上，preMacd < -0.11 && preThirdMacd < -0.15，diff都在dea下
     */
    private void strategyMacdDiffOverturn3(StockStrategy stockStrategy, TradeInfo tradeInfo, TradeInfo preTradeInfo, TradeInfo preThirdTradeInfo, TradeInfo preFourTradeInfo) {
        TradeInfo.MACD macd = tradeInfo.getMacd();
        TradeInfo.MACD preMacd = preTradeInfo.getMacd();
        TradeInfo.MACD preThirdMacd = preThirdTradeInfo.getMacd();
        TradeInfo.MACD preFourMacd = preFourTradeInfo.getMacd();

        //有拐点出现
        if (preMacd.isKeyPoint && !macd.isKeyPoint) {

            //空方向，开口向下
            if (preFourMacd.angle > 0 && preThirdMacd.angle > 0 && macd.angle < 0 && preMacd.angle < 0
                    && (preMacd.getMacd() > 0.06 && preThirdMacd.getMacd() > 0.1)
                    && (ToolMath.isFloatBig(macd.getDiff(), macd.getDea()) && ToolMath.isFloatBig(preMacd.getDiff(), preMacd.getDea()) && ToolMath.isFloatBig(preThirdMacd.getDiff(), preThirdMacd.getDea()) && ToolMath.isFloatBig(preFourMacd.getDiff(), preFourMacd.getDea()))) {
                //diff在dea线以上且每次angle差多少
                stockStrategy.addBuyBearStrategy(InitAppConstant.STRATEGORY_TYPE_MACD_DIFF_OVERTURN3);

                //多方向，开口向上
            } else if (preFourMacd.angle < 0 && preThirdMacd.angle < 0 && preMacd.angle > 0 && macd.angle > 0
                    && (preMacd.getMacd() < -0.11 && preThirdMacd.getMacd() < -0.15)
                    && (ToolMath.isFloatSmall(macd.getDiff(), macd.getDea()) && ToolMath.isFloatSmall(preMacd.getDiff(), preMacd.getDea()) && ToolMath.isFloatSmall(preThirdMacd.getDiff(), preThirdMacd.getDea()) && ToolMath.isFloatSmall(preFourMacd.getDiff(), preFourMacd.getDea()))){
                stockStrategy.addBuyBullStrategy(InitAppConstant.STRATEGORY_TYPE_MACD_DIFF_OVERTURN3);
            }
        }
    }
    /**
     * macd出现拐点，且交易量会有相应的变化
     */
    private void strategyMacdDiffOverturn5(StockStrategy stockStrategy, TradeInfo tradeInfo, TradeInfo preTradeInfo, TradeInfo preThirdTradeInfo, TradeInfo preFourTradeInfo) {
        TradeInfo.MACD macd = tradeInfo.getMacd();
        TradeInfo.MACD preMacd = preTradeInfo.getMacd();
        TradeInfo.MACD preThirdMacd = preThirdTradeInfo.getMacd();
        TradeInfo.MACD preFourMacd = preFourTradeInfo.getMacd();

        //有拐点出现
        if (preMacd.isKeyPoint && !macd.isKeyPoint ) {

            //空方向，开口向下：交易量变少
            if (preFourMacd.angle > 0 && preThirdMacd.angle > 0 && macd.angle < 0 && preMacd.angle < 0
                    && tradeInfo.getVolume() < preTradeInfo.getVolume() && tradeInfo.getVolume() < preThirdTradeInfo.getVolume()
                    && (ToolMath.isFloatBig(macd.getDiff(), macd.getDea()) && ToolMath.isFloatBig(preMacd.getDiff(), preMacd.getDea()) && ToolMath.isFloatBig(preThirdMacd.getDiff(), preThirdMacd.getDea()) && ToolMath.isFloatBig(preFourMacd.getDiff(), preFourMacd.getDea()))) {
                //diff在dea线以上且每次angle差多少
                stockStrategy.addBuyBearStrategy(InitAppConstant.STRATEGORY_TYPE_MACD_DIFF_OVERTURN5);

                //多方向，开口向上：交易量变多
            } else if (preFourMacd.angle < 0 && preThirdMacd.angle < 0 && preMacd.angle > 0 && macd.angle > 0
                    && tradeInfo.getVolume() > preTradeInfo.getVolume() && tradeInfo.getVolume() > preThirdTradeInfo.getVolume()
                    && (ToolMath.isFloatSmall(macd.getDiff(), macd.getDea()) && ToolMath.isFloatSmall(preMacd.getDiff(), preMacd.getDea()) && ToolMath.isFloatSmall(preThirdMacd.getDiff(), preThirdMacd.getDea()) && ToolMath.isFloatSmall(preFourMacd.getDiff(), preFourMacd.getDea()))){
                stockStrategy.addBuyBullStrategy(InitAppConstant.STRATEGORY_TYPE_MACD_DIFF_OVERTURN5);
            }
        }
    }

    /**
     * 策略方式1：InitAppConstant.STRATEGORY_TYPE_UTRAL_MACD
     * 看空：kdj的j>100且macd在强势区，后一时间点的kdj值和macd都小于前一时间点的值。
     * 看多：kdj的j<0且macd在弱势区，后一时间点的kdj值和macd都大于前一时间点的值。
     */
    private void strategyKdkMacd(StockStrategy stockStrategy, TradeInfo tradeInfoM5, TradeInfo preTradeInfoM5) {
        TradeInfo.KDJ preKdj = preTradeInfoM5.getKdj();
        TradeInfo.MACD preMacd = preTradeInfoM5.getMacd();

        //判断看空
        if (preKdj.getJ() >= 100 && preMacd.getMacd() > 0) {
            TradeInfo.KDJ kdj = tradeInfoM5.getKdj();
            TradeInfo.MACD macd = tradeInfoM5.getMacd();

            if (ToolMath.isFloatEqualSmall(kdj.getK(), preKdj.getK())
                    && ToolMath.isFloatEqualSmall(kdj.getJ(), preKdj.getJ())
                    && ToolMath.isFloatEqualSmall(macd.getMacd(), preMacd.getMacd())) {
                stockStrategy.addBuyBearStrategy(InitAppConstant.STRATEGORY_TYPE_UTRAL_MACD);
            }

            ToolLog.d("strategyKdkMacd1:" + tradeInfoM5.getXLabel() + " kdj:" + kdj.toString() + "  preKdj:" + preKdj.toString()
                    + " macd:" + macd.toString() + "  preMacd:" + preMacd.toString());

        } else if (preKdj.getJ() < 0 && preMacd.getMacd() < 0) {
            TradeInfo.KDJ kdj = tradeInfoM5.getKdj();
            TradeInfo.MACD macd = tradeInfoM5.getMacd();

            if (ToolMath.isFloatEqualSmall(preKdj.getK(), kdj.getK())
                    && ToolMath.isFloatEqualSmall(preKdj.getJ(), kdj.getJ())
                    && ToolMath.isFloatEqualSmall(preMacd.getMacd(), macd.getMacd())) {
                stockStrategy.addBuyBullStrategy(InitAppConstant.STRATEGORY_TYPE_UTRAL_MACD);
            }

            ToolLog.d("strategyKdkMacd1:" + tradeInfoM5.getXLabel() + " kdj:" + kdj.toString() + "  preKdj:" + preKdj.toString()
                    + " macd:" + macd.toString() + "  preMacd:" + preMacd.toString());
        }
    }

    /**
     * 策略方式2：InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT
     * 看空：kdj在超买区，k>80,d>80,j>80，在离开超买区时有交点，离开即交点在此点前
     * 看多：kdj在超卖区，k<20,d<20,j<20，在离开超卖区时有交点
     */
    private void strategyKdkIntersect(StockStrategy stockStrategy, TradeInfo tradeInfoM5, TradeInfo preTradeInfoM5) {
        TradeInfo.KDJ preKdj = preTradeInfoM5.getKdj();
        TradeInfo.KDJ kdj = tradeInfoM5.getKdj();

        //离开超买区判断
        if (preKdj.getJ() >= 80 && preKdj.getK() >= 80 && kdj.getJ() < 80 && kdj.getK() < 80 && ToolMath.isFloatEqualSmall(kdj.getD(), preKdj.getD())) {

            //前一时间段有交点，当前没有或者当前有，但是d，k值不一样（即不是右侧相交）
            if ((preKdj.hasIntersect && !kdj.hasIntersect) || (kdj.hasIntersect && !ToolMath.isFloatEqual(kdj.getD(), kdj.getK()))) {
                stockStrategy.addBuyBearStrategy(InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT);
            }

            ToolLog.d("strategyKdkMacd2:" + tradeInfoM5.getXLabel() + " kdj:" + kdj.toString() + "  preKdj:" + preKdj.toString());
        } else if (preKdj.getJ() <= 20 && preKdj.getK() <= 20 && kdj.getJ() > 20 && kdj.getK() > 20 && ToolMath.isFloatEqualBig(kdj.getD(), preKdj.getD())) {

            if ((preKdj.hasIntersect && !kdj.hasIntersect) || (kdj.hasIntersect && !ToolMath.isFloatEqual(kdj.getD(), kdj.getK()))) {
                stockStrategy.addBuyBullStrategy(InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT);
            }

            ToolLog.d("strategyKdkMacd2:" + tradeInfoM5.getXLabel() + " kdj:" + kdj.toString() + "  preKdj:" + preKdj.toString());
        }
    }


    /**
     * 策略方式3：InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT_DIRECTION
     * 看空：kdj在超买区，k>80,j>80，在离开超买区时有交点，离开即交点在此点前
     * 看多：kdj在超卖区，k<20,j<20，在离开超卖区时有交点
     */
    private void strategyKdjIntersectDirection(StockStrategy stockStrategy, TradeInfo tradeInfoM5, TradeInfo preTradeInfoM5, TradeInfo preThirdTradeInfoM5) {
        TradeInfo.KDJ preThirdKdj = preThirdTradeInfoM5.getKdj();
        TradeInfo.KDJ preKdj = preTradeInfoM5.getKdj();
        TradeInfo.KDJ kdj = tradeInfoM5.getKdj();
        String xLabel = tradeInfoM5.getXLabel();

        if (preKdj.getJ() >= 75 && preKdj.getK() >= 75 && kdj.getJ() < 80 && kdj.getK() < 80 && kdj.getD() < 80) {
            if ((preThirdKdj.hasIntersect || preKdj.hasIntersect) && !kdj.hasIntersect) {
                stockStrategy.addBuyBearStrategy(InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT_DIRECTION);
            }

            ToolLog.d("strategyKdkMacd3:" + tradeInfoM5.getXLabel() + " kdj:" + kdj.toString() + "  preKdj:" + preKdj.toString()
                    + "  preThirdKdj:" + preThirdKdj.toString());
        } else if (preKdj.getJ() <= 25 && preKdj.getK() <= 25 && kdj.getJ() > 20 && kdj.getK() > 20 && kdj.getD() > 20) {
            if ((preThirdKdj.hasIntersect || preKdj.hasIntersect) && !kdj.hasIntersect) {
                stockStrategy.addBuyBullStrategy(InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT_DIRECTION);
            }

            ToolLog.d("strategyKdkMacd3:" + tradeInfoM5.getXLabel() + " kdj:" + kdj.toString() + "  preKdj:" + preKdj.toString()
                    + "  preThirdKdj:" + preThirdKdj.toString());
        }
    }


    /**
     * 策略方式3：InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT_DIRECTION
     * 看空：kdj在超买区，k>80,d>80,j>80，在离开超买区时有交点，离开即交点在此点前
     * 看多：kdj在超卖区，k<20,d<20,j<20，在离开超卖区时有交点
     */
    private void strategyKdjIntersectDirection2(StockStrategy stockStrategy, TradeInfo tradeInfoM5, TradeInfo preTradeInfoM5, TradeInfo preThirdTradeInfoM5) {
        TradeInfo.KDJ preThirdKdj = preThirdTradeInfoM5.getKdj();
        TradeInfo.KDJ preKdj = preTradeInfoM5.getKdj();
        TradeInfo.KDJ kdj = tradeInfoM5.getKdj();

        if (preKdj.getJ() >= 75 && preKdj.getK() >= 75 && preKdj.getD() >= 75 && kdj.getJ() < 80 && kdj.getK() < 80 && kdj.getD() < 80
                && kdj.getD() - kdj.getJ() > 10) {
            if ((preThirdKdj.hasIntersect || preKdj.hasIntersect) && !kdj.hasIntersect) {
                stockStrategy.addBuyBearStrategy(InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT_DIRECTION2);
            }

            ToolLog.d("strategyKdkMacd3:" + tradeInfoM5.getXLabel() + " kdj:" + kdj.toString() + "  preKdj:" + preKdj.toString()
                    + "  preThirdKdj:" + preThirdKdj.toString());
        } else if (preKdj.getJ() <= 25 && preKdj.getK() <= 25 && preKdj.getD() <= 25 && kdj.getJ() > 20 && kdj.getK() > 20 && kdj.getD() > 20
                && kdj.getJ() - kdj.getD() > 10) {
            if ((preThirdKdj.hasIntersect || preKdj.hasIntersect) && !kdj.hasIntersect) {
                stockStrategy.addBuyBullStrategy(InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT_DIRECTION2);
            }

            ToolLog.d("strategyKdkMacd3:" + tradeInfoM5.getXLabel() + " kdj:" + kdj.toString() + "  preKdj:" + preKdj.toString()
                    + "  preThirdKdj:" + preThirdKdj.toString());
        }
    }

    /**
     * 条件严格的策略2
     * 策略方式4：InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT_DIRECTION
     * 看空：kdj在超买区，k>80,d>80,j>80，在离开超买区时有交点，离开即交点在此点前
     * 看多：kdj在超卖区，k<20,d<20,j<20，在离开超卖区时有交点
     */
    private void strategyKdjIntersectDirection3(StockStrategy stockStrategy, TradeInfo tradeInfoM5, TradeInfo preTradeInfoM5, TradeInfo preThirdTradeInfoM5) {
        TradeInfo.KDJ preThirdKdj = preThirdTradeInfoM5.getKdj();
        TradeInfo.KDJ preKdj = preTradeInfoM5.getKdj();
        TradeInfo.KDJ kdj = tradeInfoM5.getKdj();

        if ((preKdj.hasIntersect || preThirdKdj.hasIntersect) && !kdj.hasIntersect) {

            if (ToolMath.isFloatEqualBig(kdj.getK(), preKdj.getK()) && ToolMath.isFloatEqualBig(preKdj.getK(), preThirdKdj.getK())
                    && ToolMath.isFloatEqualBig(kdj.getJ(), preKdj.getJ()) && ToolMath.isFloatEqualBig(preKdj.getJ(), preThirdKdj.getJ())
                    && preThirdKdj.getJ() > 10  && preThirdKdj.getJ() < 20 && kdj.getJ() > 25
                    && (preKdj.getD() <= 20 || preThirdKdj.getD() <= 20)
                    && kdj.getJ() - kdj.getD() > 10) {
                stockStrategy.addBuyBullStrategy(InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT_DIRECTION3);

            } else if (ToolMath.isFloatEqualSmall(kdj.getK(), preKdj.getK()) && ToolMath.isFloatEqualSmall(preKdj.getK(), preThirdKdj.getK())
                    && ToolMath.isFloatEqualSmall(kdj.getJ(), preKdj.getJ()) && ToolMath.isFloatEqualSmall(preKdj.getJ(), preThirdKdj.getJ())
                    && kdj.getJ() < 75 && preThirdKdj.getJ() < 90 && preThirdKdj.getJ() > 80
                    && (preKdj.getD() >= 80 || preThirdKdj.getD() >= 80)
                    && kdj.getD() - kdj.getJ() > 10) {
                stockStrategy.addBuyBearStrategy(InitAppConstant.STRATEGORY_TYPE_UTRAL_INTERSECT_DIRECTION3);
            }


            ToolLog.d("strategyKdkMacd3:" + tradeInfoM5.getXLabel() + " kdj:" + kdj.toString() + "  preKdj:" + preKdj.toString()
                    + "  preThirdKdj:" + preThirdKdj.toString());
        }
    }

    /**
     * 结合boll的强弱区及macd的金死叉来判断买点
     * 超买：在boll的强势区，macd发生了死叉，且交后macd两条线都向下
     * 超卖：在boll的弱势区，macd发生了金叉，且交后macd两条线都向上
     * @param tradeInfoM5
     * @param preTradeInfoM5
     */
    private void strategyBollMacd(StockStrategy stockStrategy, TradeInfo tradeInfoM5, TradeInfo preTradeInfoM5, TradeInfo prThirdeTradeInfoM5) {
        TradeInfo.MACD macd = tradeInfoM5.getMacd();
        TradeInfo.MACD preMacd = preTradeInfoM5.getMacd();

        String date = tradeInfoM5.getXLabel();
        if (macd.hasIntersect) {
            TradeInfo.Boll boll = tradeInfoM5.getBoll();
            TradeInfo.Boll preBoll = preTradeInfoM5.getBoll();
            TradeInfo.Boll preThirdBoll = prThirdeTradeInfoM5.getBoll();

            //看空
            if (preMacd.getMacd() > 0 && macd.getMacd() < 0
                    && preBoll.getB() > 0.5 && preThirdBoll.getB() > 0.5
                    && (boll.getB() > 0.5 || (tradeInfoM5.getOpen() > boll.getMb() && tradeInfoM5.getClose() < boll.getMb() && tradeInfoM5.getClose() > boll.getDn()))
                            /*&& ((preBoll.getB() >= 0.55 && preThirdBoll.getB() >= 0.55)
                                || (preTradeInfoM5.getHigh() > preBoll.getMb() && preTradeInfoM5.getLow() < preBoll.getMb() && preThirdBoll.getB() >= 0.55))*/) {
                stockStrategy.addBuyBearStrategy(InitAppConstant.STRATEGORY_TYPE_BOLL_ZONE_MACD_INTERSECT);

                //看多
            } else if (macd.getMacd() > 0 && preMacd.getMacd() < 0
                    && preBoll.getB() < 0.5 && preThirdBoll.getB() < 0.5
                    && ((boll.getB() < 0.5 || (tradeInfoM5.getClose() > boll.getMb() && tradeInfoM5.getOpen() < boll.getMb() && tradeInfoM5.getClose() < boll.getUp()))
                            /*&& ((preBoll.getB() <= 0.45 && preThirdBoll.getB() <= 0.45)
                                ||(preTradeInfoM5.getHigh() > preBoll.getMb() && preTradeInfoM5.getLow() < preBoll.getMb() && preThirdBoll.getB() <= 0.45))*/)) {
                stockStrategy.addBuyBullStrategy(InitAppConstant.STRATEGORY_TYPE_BOLL_ZONE_MACD_INTERSECT);
            }
        }
    }
    /**
     * 对最新数据（列表末尾项）分析各项指标
     * @param values  针对的是某一支的某一分钟
     * @param tmpData 针对的是某一支的某一分钟
     */
    private synchronized void analyzeNewIndex(String symbol, List<TradeInfo> values, AnalyzeTmpData.TmpData tmpData) {
        MAInd.INSTANCE.computeMANew(tmpData.maTmp, values);
        MACDInd.INSTANCE.computeMACDNew(tmpData.macdTmp, values);
        BollInd.INSTANCE.computeBollNew(tmpData.bollTmp, values);
        KDJInd.INSTANCE.computeKDJNew(tmpData.kdjTmp, values);
        RSIInd.INSTANCE.computeRSINew(tmpData.rsiTmp, values);
        OBVInd.INSTANCE.computeOBVNew(tmpData.obvTmp, values);
    }

    /**
     * 对历史数据分析各项指标
     * period  下次要请求的数据
     * @param values
     */
    public synchronized long analyzeHistIndex(int type, long serverTime, String symbol, List<TradeInfo> values) {
        //根据服务器时间计算时间段
        mFirstPeriodStart = ToolTime.getServerStartTime(serverTime, 21, 30);
        mFirstPeriodEnd = mFirstPeriodStart + ToolTime.getTimeDiffMillis(0, 22);

        //下次请求时间是最后一项的时间，获取后移除
        int lastPos = values.size() - 1;
        long period = values.get(lastPos).getTime();
        values.remove(lastPos);


        //开始循环计算各项指标
        AnalyzeTmpData.TmpData tmpData = new AnalyzeTmpData.TmpData();

        MAInd.INSTANCE.computeMAHistory(tmpData.maTmp, values);
        MACDInd.INSTANCE.computeMACDHistory(tmpData.macdTmp, values);
        BollInd.INSTANCE.computeBollHistory(tmpData.bollTmp, values);
        KDJInd.INSTANCE.computeKDJHistory(tmpData.kdjTmp, values);
        RSIInd.INSTANCE.computeRSIHistory(tmpData.rsiTmp, values);
        OBVInd.INSTANCE.computeOBVHistory(tmpData.obvTmp, values);

        //将计算的中间值保存到对应的symbol的相应时间周期上
        AnalyzeTmpData newAnalyzeTmpData = null;
        if (mAnalyTmpData.containsKey(symbol)) {
            newAnalyzeTmpData = mAnalyTmpData.get(symbol);
        } else {
            newAnalyzeTmpData = new AnalyzeTmpData();
            mAnalyTmpData.put(symbol, newAnalyzeTmpData);
        }
        switch (type) {
            case InitAppConstant.MINUTE_1:
                newAnalyzeTmpData.setM1(tmpData);
                break;
            case InitAppConstant.MINUTE_5:
                newAnalyzeTmpData.setM5(tmpData);
                break;
            case InitAppConstant.MINUTE_15:
                newAnalyzeTmpData.setM15(tmpData);
                break;
            case InitAppConstant.MINUTE_30:
                newAnalyzeTmpData.setM30(tmpData);
                break;
            case InitAppConstant.MINUTE_60:
                newAnalyzeTmpData.setM60(tmpData);
                break;
            default:
                break;
        }

        return period;
    }

}
