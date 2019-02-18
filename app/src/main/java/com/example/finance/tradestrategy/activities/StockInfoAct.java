package com.example.finance.tradestrategy.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.finance.tradestrategy.BR;
import com.example.finance.tradestrategy.R;
import com.example.finance.tradestrategy.baseui.BaseActivity;
import com.example.finance.tradestrategy.customviews.ToolPopupWindow;
import com.example.finance.tradestrategy.databinding.ActStockInfoBinding;
import com.example.finance.tradestrategy.databindings.DatabindingUtls;
import com.example.finance.tradestrategy.entity.BaseResponse;
import com.example.finance.tradestrategy.entity.ItemSelectEntity;
import com.example.finance.tradestrategy.entity.NetCallback;
import com.example.finance.tradestrategy.entity.StockInfo;
import com.example.finance.tradestrategy.entity.TradeInfo;
import com.example.finance.tradestrategy.globaldata.InitAppConstant;
import com.example.finance.tradestrategy.globaldata.InitNetInfo;
import com.example.finance.tradestrategy.globaldata.MessageId;
import com.example.finance.tradestrategy.indicators.calculate.AnalyzeIndTest;
import com.example.finance.tradestrategy.indicators.view.KLineRender;
import com.example.finance.tradestrategy.indicators.view.OnLoadingData;
import com.example.finance.tradestrategy.indicators.view.TradeInfoSet;
import com.example.finance.tradestrategy.indicators.view.drawing.HighlightDrawing;
import com.example.finance.tradestrategy.indicators.view.drawing.KDJDrawing;
import com.example.finance.tradestrategy.indicators.view.drawing.MACDDrawing;
import com.example.finance.tradestrategy.indicators.view.drawing.RSIDrawing;
import com.example.finance.tradestrategy.indicators.view.drawing.StockIndexYLabelDrawing;
import com.example.finance.tradestrategy.indicators.view.index.StockKDJIndex;
import com.example.finance.tradestrategy.indicators.view.index.StockMACDIndex;
import com.example.finance.tradestrategy.indicators.view.index.StockRSIIndex;
import com.example.finance.tradestrategy.indicators.view.maker.DetailYAxisTextMarkerView;
import com.example.finance.tradestrategy.utils.ToolApp;
import com.example.finance.tradestrategy.utils.ToolLog;
import com.example.finance.tradestrategy.utils.ToolRequest;
import com.example.finance.tradestrategy.utils.ToolTime;
import com.example.finance.tradestrategy.utils.ToolToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/12.
 */

public class StockInfoAct extends BaseActivity implements NetCallback, OnLoadingData, View.OnClickListener {

    private ActStockInfoBinding mBinding;
    private KLineRender kLineRender;
    private StockInfo   mStockInfo;
    private String      mSymbol;
    //请求新纪录的时间段
    private long        mNewPeriod = -1;
    //请求历史记录的时间段
    private long        mHistoryPeriod;
    //存储不同时间周期的历史请求时间点Map<PERIOD_CANDLE_PREFIX_MINUTE + period，历史时间段>
    private Map<String, Long>   mHistoryPeriods = new ArrayMap<>(5);

    //可供选择策略类型
    private List<ItemSelectEntity>  mStrategories;
    //可供选择时间周期
    private List<ItemSelectEntity>  mPeriods;
    //选择的时间周期
    private String      mSelectPeriod;
    private String      mHisData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.act_stock_info);
        mSymbol = getIntent().getStringExtra("symbol");


        initUI();
        makeDefaultSel();
    }

    private void makeDefaultSel() {
        //初始化策略组
        mStrategories = new ArrayList<>(10);
        for (int i = InitAppConstant.STRATEGORY_TYPE_START; i < InitAppConstant.STRATEGORY_TYPE_END; ++ i) {
            //如果策略名为空，不加入选项列表
            String type = DatabindingUtls.getStrategoryType(i);
            if (TextUtils.isEmpty(type)) {
                continue;
            }

            //初始化STRATEGORY_TYPE_BOLL_ZONE_MACD_INTERSECT3为默认选择的策略
            if (InitAppConstant.STRATEGORY_TYPE_BOLL_ZONE_MACD_INTERSECT3 == i) {
                mStrategories.add(new ItemSelectEntity(true, "策略" + type, i));
            } else {
                mStrategories.add(new ItemSelectEntity(false, "策略" + type, i));
            }

        }
        showStrategyTypeSelection(mBinding.strategyType, mStrategories, false);

        //初始化周期
        mPeriods = new ArrayList<>(6);
        mPeriods.add(new ItemSelectEntity(false, "1min", 1));  //InitNetInfo.PERIOD_CANDLE_1_MINUTE));
        mPeriods.add(new ItemSelectEntity(true,  "5min", 5));  //InitNetInfo.PERIOD_CANDLE_5_MINUTE));
        mPeriods.add(new ItemSelectEntity(false, "15min", 15));//InitNetInfo.PERIOD_CANDLE_15_MINUTE));
        mPeriods.add(new ItemSelectEntity(false, "30min", 30));//InitNetInfo.PERIOD_CANDLE_30_MINUTE));
        mPeriods.add(new ItemSelectEntity(false, "60min", 60));//InitNetInfo.PERIOD_CANDLE_60_MINUTE));
        showPeriodSelection(mBinding.period, mPeriods);
    }


    private void initUI() {
        mBinding.setVariable(BR.onBindClick, this);
        mBinding.setVariable(BR.symbol, mSymbol);

        mBinding.kLineView.setOnLoadingData(this);
        mBinding.kLineView.setEnableLeftRefresh(false);
        mBinding.kLineView.setEnableLeftRefresh(false);
        kLineRender = (KLineRender) mBinding.kLineView.getRender();

        final int paddingTop = ToolApp.dpTopx(this, 10);
        final int stockMarkerViewHeight = ToolApp.dpTopx(this, 15);

        // MACD
        HighlightDrawing macdHighlightDrawing = new HighlightDrawing();
        macdHighlightDrawing.addMarkerView(new DetailYAxisTextMarkerView(stockMarkerViewHeight));

        StockMACDIndex macdIndex = new StockMACDIndex();
        macdIndex.addDrawing(new MACDDrawing());
        macdIndex.addDrawing(new StockIndexYLabelDrawing());
        macdIndex.addDrawing(macdHighlightDrawing);
        macdIndex.setPaddingTop(paddingTop);
        kLineRender.addStockIndex(macdIndex);

        // KDJ
        HighlightDrawing kdjHighlightDrawing = new HighlightDrawing();
        kdjHighlightDrawing.addMarkerView(new DetailYAxisTextMarkerView(stockMarkerViewHeight));

        StockKDJIndex kdjIndex = new StockKDJIndex();
        kdjIndex.addDrawing(new KDJDrawing());
        kdjIndex.addDrawing(new StockIndexYLabelDrawing());
        kdjIndex.addDrawing(kdjHighlightDrawing);
        kdjIndex.setPaddingTop(paddingTop);
        kLineRender.addStockIndex(kdjIndex);


        // RSI
        HighlightDrawing rsiHighlightDrawing = new HighlightDrawing();
        rsiHighlightDrawing.addMarkerView(new DetailYAxisTextMarkerView(stockMarkerViewHeight));

        StockRSIIndex rsiIndex = new StockRSIIndex();
        rsiIndex.addDrawing(new RSIDrawing());
        rsiIndex.addDrawing(new StockIndexYLabelDrawing());
        rsiIndex.addDrawing(rsiHighlightDrawing);
        rsiIndex.setPaddingTop(paddingTop);
        kLineRender.addStockIndex(rsiIndex);

//        //OBV
//        HighlightDrawing obvHighlightDrawing = new HighlightDrawing();
//        obvHighlightDrawing.addMarkerView(new DetailYAxisTextMarkerView(stockMarkerViewHeight));
//
//        StockOBVIndex obvIndex = new StockOBVIndex();
//        obvIndex.addDrawing(new OBVDrawing());
//        obvIndex.addDrawing(new StockIndexYLabelDrawing());
//        obvIndex.addDrawing(obvHighlightDrawing);
//        obvIndex.setPaddingTop(paddingTop);
//        kLineRender.addStockIndex(obvIndex);


        //todo:x、y信息都不用显示了，Detail中会包含且扩充了
//        kLineRender.addMarkerView(new YAxisTextMarkerView(stockMarkerViewHeight));
//        kLineRender.addMarkerView(new XAxisTextMarkerView(stockMarkerViewHeight));
        kLineRender.addMarkerView(new DetailYAxisTextMarkerView(stockMarkerViewHeight));
    }

    private void loadData(List<TradeInfo> tradeInfoList) {
        TradeInfoSet tradeInfoSet = new TradeInfoSet();
        tradeInfoSet.addEntries(tradeInfoList);
        mBinding.kLineView.setTradeInfoSet(tradeInfoSet);
        mBinding.kLineView.notifyDataSetChanged();
    }


    @Override
    public boolean handleMessage(Message msg) {
        if (super.handleMessage(msg)) {
            return true;
        }

        switch (msg.what) {
            case MessageId.UPDATE_DATA:
                if (InitNetInfo.PERIOD_CANDLE_1_MINUTE.equals(msg.obj)) {
                    loadData(mStockInfo.getItems());
                } else if (InitNetInfo.PERIOD_CANDLE_5_MINUTE.equals(msg.obj)) {
                    loadData(mStockInfo.getItems5());
                } else if (InitNetInfo.PERIOD_CANDLE_15_MINUTE.equals(msg.obj)) {
                    loadData(mStockInfo.getItems15());
                } else if (InitNetInfo.PERIOD_CANDLE_30_MINUTE.equals(msg.obj)) {
                    loadData(mStockInfo.getItems30());
                } else if (InitNetInfo.PERIOD_CANDLE_60_MINUTE.equals(msg.obj)) {
                    loadData(mStockInfo.getItems60());
                }

                mBinding.kLineView.setIsLoading(false);
                break;
        }

        return true;
    }

    @Override
    public void onError(String method, int connCode, String data) {
        packMsgAndSend(MessageId.TOAST_TIP, data);
    }

    @Override
    public void onSuccess(String method, BaseResponse data) {
        if (null == data) {
            ToolLog.e(TAG, "onSuccess", method,  "data is null");
            return;
        }
        StockInfo stockInfo = null;

        switch (method) {
            case InitNetInfo.PERIOD_CANDLE_5_MINUTE:
                stockInfo = (StockInfo) data;

                //当数据项数大于3就代表是历史数据
                if (stockInfo.getItems().size() > 3) {
                    stockInfo.setItems5(stockInfo.getItems());
                    stockInfo.setItems(null);

                    //detail为空表示是拉去某时间点之前数据
                    if (null == stockInfo.getDetail()) {
                        mStockInfo.addItems5Head(stockInfo.getItems5());
                    } else if (null == mStockInfo){
                        mStockInfo = stockInfo;
                    } else {
                        mStockInfo.setItems5(stockInfo.getItems5());
                    }

                    mHistoryPeriods.put(InitNetInfo.PERIOD_CANDLE_5_MINUTE, mStockInfo.getItems5().get(0).getTime());
                    mNewPeriod = AnalyzeIndTest.INSTANCE.analyzeHistByAdd(InitAppConstant.MINUTE_5, stockInfo.getServerTime(), stockInfo.getSymbol(), mStockInfo.getItems5());

                    packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_5_MINUTE);
                }
                break;

            case InitNetInfo.PERIOD_CANDLE_1_MINUTE:
                stockInfo = (StockInfo) data;

                //当数据项数大于3就代表是历史数据
                if (stockInfo.getItems().size() > 3) {
                    //detail为空表示是拉去某时间点之前数据
                    if (null == stockInfo.getDetail()) {
                        mStockInfo.addItemsHead(stockInfo.getItems());
                    } else if (null == mStockInfo){
                        mStockInfo = stockInfo;
                    } else {
                        mStockInfo.setItems(stockInfo.getItems());
                    }

                    mHistoryPeriods.put(InitNetInfo.PERIOD_CANDLE_1_MINUTE, mStockInfo.getItems().get(0).getTime());
                    mNewPeriod = AnalyzeIndTest.INSTANCE.analyzeHistByAdd(InitAppConstant.MINUTE_1, stockInfo.getServerTime(), stockInfo.getSymbol(), mStockInfo.getItems());

                    packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_1_MINUTE);
                }
                break;
            case InitNetInfo.PERIOD_CANDLE_15_MINUTE:
                stockInfo = (StockInfo) data;

                //当数据项数大于3就代表是历史数据
                if (stockInfo.getItems().size() > 3) {
                    stockInfo.setItems15(stockInfo.getItems());
                    stockInfo.setItems(null);

                    //detail为空表示是拉去某时间点之前数据
                    if (null == stockInfo.getDetail()) {
                        mStockInfo.addItems15Head(stockInfo.getItems15());
                    } else if (null == mStockInfo){
                        mStockInfo = stockInfo;
                    } else {
                        mStockInfo.setItems15(stockInfo.getItems15());
                    }

                    mHistoryPeriods.put(InitNetInfo.PERIOD_CANDLE_15_MINUTE, mStockInfo.getItems15().get(0).getTime());
                    mNewPeriod = AnalyzeIndTest.INSTANCE.analyzeHistByAdd(InitAppConstant.MINUTE_15, stockInfo.getServerTime(), stockInfo.getSymbol(), mStockInfo.getItems15());

                    packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_15_MINUTE);
                }
                break;
            case InitNetInfo.PERIOD_CANDLE_30_MINUTE:
                stockInfo = (StockInfo) data;

                //当数据项数大于3就代表是历史数据
                if (stockInfo.getItems().size() > 3) {
                    stockInfo.setItems30(stockInfo.getItems());
                    stockInfo.setItems(null);

                    //detail为空表示是拉去某时间点之前数据
                    if (null == stockInfo.getDetail()) {
                        mStockInfo.addItems30Head(stockInfo.getItems30());
                    } else if (null == mStockInfo){
                        mStockInfo = stockInfo;
                    } else {
                        mStockInfo.setItems30(stockInfo.getItems30());
                    }

                    mHistoryPeriods.put(InitNetInfo.PERIOD_CANDLE_30_MINUTE, mStockInfo.getItems30().get(0).getTime());
                    mNewPeriod = AnalyzeIndTest.INSTANCE.analyzeHistByAdd(InitAppConstant.MINUTE_30, stockInfo.getServerTime(), stockInfo.getSymbol(), mStockInfo.getItems30());

                    packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_30_MINUTE);
                }
                break;
            case InitNetInfo.PERIOD_CANDLE_60_MINUTE:
                stockInfo = (StockInfo) data;

                //当数据项数大于3就代表是历史数据
                if (stockInfo.getItems().size() > 3) {
                    stockInfo.setItems60(stockInfo.getItems());
                    stockInfo.setItems(null);

                    //detail为空表示是拉去某时间点之前数据
                    if (null == stockInfo.getDetail()) {
                        mStockInfo.addItems60Head(stockInfo.getItems60());
                    } else if (null == mStockInfo){
                        mStockInfo = stockInfo;
                    } else {
                        mStockInfo.setItems60(stockInfo.getItems60());
                    }

                    mHistoryPeriods.put(InitNetInfo.PERIOD_CANDLE_60_MINUTE, mStockInfo.getItems60().get(0).getTime());
                    mNewPeriod = AnalyzeIndTest.INSTANCE.analyzeHistByAdd(InitAppConstant.MINUTE_60, stockInfo.getServerTime(), stockInfo.getSymbol(), mStockInfo.getItems60());

                    packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_60_MINUTE);
                }
                break;
        }
    }

    @Override
    public void OnHistoryLoadingMore() {
        if (!ToolTime.isReachTimeMS(mHisData, 10000)) {
            ToolToast.shortToast(this, "刷新过频繁，稍后再试");
            return;
        }

        String period = InitNetInfo.PERIOD_CANDLE_PREFIX_MINUTE + mSelectPeriod;
        ToolRequest.getInstance().getStockInfoBefore(period, mSymbol, mHistoryPeriods.get(period), this);
    }

    @Override
    public void OnNewLoadingMore() {
        ToolToast.shortToast(this, "加载更多最新");
//
//        //删除最新的文件
//        String period = InitNetInfo.PERIOD_CANDLE_PREFIX_MINUTE + mSelectPeriod;
//        String fileName = ToolFile.makeName(period, mSymbol, -1);
//        ToolFile.removeFile(ToolFile.getFilePath() + fileName);
//
//
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.strategyType:
                ToolPopupWindow.INSTANCE.showMultipleSelectList(this, v, new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        showStrategyTypeSelection((TextView) v, mStrategories, true);
                    }
                }, mStrategories);
                break;
            case R.id.period:
                ToolPopupWindow.INSTANCE.showSingleSelectList(this, v, new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        showPeriodSelection((TextView) v, mPeriods);
                    }
                }, mPeriods);

                break;
        }
    }

    /**
     * 策略选项
     * @param v
     * @param items
     * @param isReload 是否第一次加载，true不是，false是
     */
    private void showStrategyTypeSelection(TextView v, List<ItemSelectEntity> items, boolean isReload) {
        StringBuilder builder = new StringBuilder();
        AnalyzeIndTest.INSTANCE.mStrategyTypes.clear();
        for (ItemSelectEntity item : items) {
            if (item.isChecked()) {
                builder.append(item.getValue());
                AnalyzeIndTest.INSTANCE.mStrategyTypes.add(item.getValueInt());
            }
        }

        //显示选择策略
        String typeTip = "策略";
        if (!TextUtils.isEmpty(builder.toString())) {
            typeTip += builder.toString().replace("策略", "，").substring(1);
        }
        mBinding.strategyType.setText(typeTip);


        //重新加载分析策略
        if (isReload) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    switch (InitNetInfo.PERIOD_CANDLE_PREFIX_MINUTE + mSelectPeriod) {
                        case InitNetInfo.PERIOD_CANDLE_1_MINUTE:
                            AnalyzeIndTest.INSTANCE.analyzeHistByAdd(InitAppConstant.MINUTE_1, 0, mStockInfo.getSymbol(), mStockInfo.getItems());
                            packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_1_MINUTE);
                            break;
                        case InitNetInfo.PERIOD_CANDLE_5_MINUTE:
                            AnalyzeIndTest.INSTANCE.analyzeHistByAdd(InitAppConstant.MINUTE_5, 0, mStockInfo.getSymbol(), mStockInfo.getItems5());
                            packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_5_MINUTE);
                            break;
                        case InitNetInfo.PERIOD_CANDLE_15_MINUTE:
                            AnalyzeIndTest.INSTANCE.analyzeHistByAdd(InitAppConstant.MINUTE_15, 0, mStockInfo.getSymbol(), mStockInfo.getItems15());
                            packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_15_MINUTE);
                            break;
                        case InitNetInfo.PERIOD_CANDLE_30_MINUTE:
                            AnalyzeIndTest.INSTANCE.analyzeHistByAdd(InitAppConstant.MINUTE_30, 0, mStockInfo.getSymbol(), mStockInfo.getItems30());
                            packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_30_MINUTE);
                            break;
                        case InitNetInfo.PERIOD_CANDLE_60_MINUTE:
                            AnalyzeIndTest.INSTANCE.analyzeHistByAdd(InitAppConstant.MINUTE_60, 0, mStockInfo.getSymbol(), mStockInfo.getItems60());
                            packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_60_MINUTE);
                            break;
                    }
                }
            }).start();
        }
    }

    private void showPeriodSelection(TextView v, List<ItemSelectEntity> items) {
        for (ItemSelectEntity item : items) {
            //如果时间周期没变动，不重新加载
            if (item.isChecked() && !item.getValue().equals(mSelectPeriod)) {
                mSelectPeriod = item.getValue();
                resetPeriod();
                break;
            }
        }
    }

    //重新请求新的周期数据
    private void resetPeriod() {
        mBinding.period.setText(mSelectPeriod);

        mNewPeriod = -1;

        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (InitNetInfo.PERIOD_CANDLE_PREFIX_MINUTE + mSelectPeriod) {
                    case InitNetInfo.PERIOD_CANDLE_1_MINUTE:
                        if (null != mStockInfo && null != mStockInfo.getItems()) {
                            packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_1_MINUTE);
                        } else {
                            ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_1_MINUTE, mSymbol, mNewPeriod, StockInfoAct.this);
                        }
                        break;
                    case InitNetInfo.PERIOD_CANDLE_5_MINUTE:
                        if (null != mStockInfo && null != mStockInfo.getItems5()) {
                            packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_5_MINUTE);
                        } else {
                            ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_5_MINUTE, mSymbol, mNewPeriod, StockInfoAct.this);
                        }

                        break;
                    case InitNetInfo.PERIOD_CANDLE_15_MINUTE:
                        if (null != mStockInfo && null != mStockInfo.getItems15()) {
                            packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_15_MINUTE);
                        } else {
                            ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_15_MINUTE, mSymbol, mNewPeriod, StockInfoAct.this);
                        }

                        break;
                    case InitNetInfo.PERIOD_CANDLE_30_MINUTE:
                        if (null != mStockInfo && null != mStockInfo.getItems30()) {
                            packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_30_MINUTE);
                        } else {
                            ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_30_MINUTE, mSymbol, mNewPeriod, StockInfoAct.this);
                        }

                        break;
                    case InitNetInfo.PERIOD_CANDLE_60_MINUTE:
                        if (null != mStockInfo && null != mStockInfo.getItems60()) {
                            packMsgAndSend(MessageId.UPDATE_DATA, InitNetInfo.PERIOD_CANDLE_60_MINUTE);
                        } else {
                            ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_60_MINUTE, mSymbol, mNewPeriod, StockInfoAct.this);
                        }

                        break;
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToolTime.removeTimeTag(mHisData);
    }
}
