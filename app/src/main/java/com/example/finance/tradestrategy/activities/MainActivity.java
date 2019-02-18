package com.example.finance.tradestrategy.activities;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.android.annotations.Nullable;
import com.example.finance.tradestrategy.R;
import com.example.finance.tradestrategy.adapters.BaseViewpagerAdapter;
import com.example.finance.tradestrategy.base.BaseApplication;
import com.example.finance.tradestrategy.base.PresenterManager;
import com.example.finance.tradestrategy.baseui.BaseActivity;
import com.example.finance.tradestrategy.baseui.BaseFragment;
import com.example.finance.tradestrategy.databinding.ActMainBinding;
import com.example.finance.tradestrategy.entity.BaseResponse;
import com.example.finance.tradestrategy.entity.NetCallback;
import com.example.finance.tradestrategy.entity.RequestPeriod;
import com.example.finance.tradestrategy.entity.StockInfo;
import com.example.finance.tradestrategy.entity.StockStrategy;
import com.example.finance.tradestrategy.fragments.ForecastRecordFrag;
import com.example.finance.tradestrategy.fragments.IndicatorDetailFrag;
import com.example.finance.tradestrategy.globaldata.InitAppConstant;
import com.example.finance.tradestrategy.globaldata.InitData;
import com.example.finance.tradestrategy.globaldata.InitNetInfo;
import com.example.finance.tradestrategy.globaldata.MessageId;
import com.example.finance.tradestrategy.indicators.calculate.AnalyzeInd;
import com.example.finance.tradestrategy.utils.ToolLog;
import com.example.finance.tradestrategy.utils.ToolNotification;
import com.example.finance.tradestrategy.utils.ToolRequest;
import com.example.finance.tradestrategy.utils.ToolSharePre;
import com.example.finance.tradestrategy.utils.ToolTime;
import com.example.finance.tradestrategy.utils.ToolToast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements NetCallback{
    private ActMainBinding mActBinding;

    //菜单项
    private Menu    mMainMenu;

    private ForecastRecordFrag mForecastRecordFra;
    private IndicatorDetailFrag mIndicatorDetailFra;

    //adapter对应的数据，用于显示
    private LinkedHashMap<String, StockStrategy> mStockStrategy = new LinkedHashMap<>(10);

    //所有项的列表数据，用于分析计算
    private Map<String, StockInfo>    mStockInfos;  //stockcode， StockInfo
    private Map<String, RequestPeriod>   mRequestPeriod;   //各股票的请求时间段

    private Timer       mTimer;
    private long        mPeriod = 6000;    //timer循环间隔周期6s
    private long        mPeriod5  = 0;      //5分更新时刻
    private long        mPeriod15  = 0;     //15分更新时刻
    private final int   UPDATE_PERIOD = 30000;     //5分、15分更新周期
    private boolean     mIsStart = false;   //是否开始

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActBinding = DataBindingUtil.setContentView(this, R.layout.act_main);


        ToolRequest.getInstance().getStockInfoCookie();
        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //更新市场时间
        ToolTime.initTime();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mMainMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                PresenterManager.toSetting(this, 1);
                break;
            case R.id.startOrStop:
                mHandler.sendEmptyMessage(MessageId.UPDATE_START_STOP);
                break;
        }

        return true;
    }

    /**
     * 测试数据
     */
    private boolean test() {

        //
        Intent intent = new Intent(MainActivity.this, StockInfoAct.class);
        intent.putExtra("symbol", "AMD");
        startActivity(intent);
        if (true) {
            return true;
        }

        mStockInfos = new ArrayMap<>(BaseApplication.mStockCodes.size());
        mRequestPeriod = new ArrayMap<>(BaseApplication.mStockCodes.size());
//        ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_1_MINUTE, "AMD", -1, this);
        ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_5_MINUTE, "AMD", -1, this);
//        ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_15_MINUTE, "AMD", -1, this);
        if (true) {
            return true;
        }

        for (int i = 0; i < 7; ++i) {
            final String symbol = "AA" + i;
            final StockStrategy stockStrategy = new StockStrategy("阿里", symbol, 0);
            stockStrategy.resetBuy();

            mStockStrategy.put(symbol, stockStrategy);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ToolNotification.getInstance().showNotification(stockStrategy);
                    packMsgAndSend(MessageId.FORECAST_RECORD_ADD, stockStrategy);
                    packMsgAndSend(MessageId.FORECAST_DATA_ADD_CODE, symbol);
                }
            }).start();

        }

        return true;
    }

    private void initView() {

        //页面
        mForecastRecordFra = new ForecastRecordFrag();
        mIndicatorDetailFra = new IndicatorDetailFrag();

        List<BaseFragment> fragments = new ArrayList<>(3);
        fragments.add(0, mIndicatorDetailFra);
        fragments.add(1, mForecastRecordFra);

        mActBinding.dataContainer.setOffscreenPageLimit(3);
        mActBinding.dataContainer.setAdapter(new BaseViewpagerAdapter<BaseFragment>(getSupportFragmentManager(), fragments));
        mActBinding.dataContainer.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

            }
        });

        //添加股票列表
        for (String symbol : BaseApplication.mStockCodes) {
            if (!mStockStrategy.containsKey(symbol)) {
                mStockStrategy.put(symbol, new StockStrategy(symbol, symbol, mStockStrategy.size()));
                //界面添加新项
                packDelayMsgAndSend(MessageId.FORECAST_DATA_ADD_CODE, symbol, 2000);
            }
        }
    }

    @Override
    public void onSuccess(String method, BaseResponse data) {
        if (null == data) {
            ToolLog.e(TAG, "onSuccess", method,  "data is null");
            return;
        }
        StockInfo stockInfo = null;

        switch (method) {
            case InitNetInfo.PERIOD_CANDLE_1_MINUTE:
                stockInfo = (StockInfo)data;

                //当数据项数大于3就代表是历史数据
                if (stockInfo.getItems().size() > 3) {
                    if (mStockInfos.containsKey(stockInfo.getSymbol())) {
                        mStockInfos.get(stockInfo.getSymbol()).setItems(stockInfo.getItems());
                        stockInfo = mStockInfos.get(stockInfo.getSymbol());
                    } else {
                        mStockInfos.put(stockInfo.getSymbol(), stockInfo);
                    }
                    long period1 = AnalyzeInd.INSTANCE.analyzeHistIndex(InitAppConstant.MINUTE_1, stockInfo.getServerTime(), stockInfo.getSymbol(), stockInfo.getItems());
                    mRequestPeriod.get(stockInfo.getSymbol()).m1 = period1;

                    //更新列表名称
                    mStockStrategy.get(stockInfo.getSymbol()).setNameCN(stockInfo.getDetail().getNameCN());
                } else {
                    if (mStockInfos.containsKey(stockInfo.getSymbol())) {
                        long period1 = mRequestPeriod.get(stockInfo.getSymbol()).m1;
                        StockStrategy stockStrategy = mStockStrategy.get(stockInfo.getSymbol());
                        period1 = AnalyzeInd.INSTANCE.analyzeStockInfoResult(InitAppConstant.MINUTE_1, mStockInfos.get(stockInfo.getSymbol()),
                                stockInfo, period1, stockStrategy);
                        mRequestPeriod.get(stockInfo.getSymbol()).m1 = period1;

                        //添加通知和记录
                        if (stockStrategy.isBuy() || stockStrategy.isSell()) {
                            ToolNotification.getInstance().showNotification(stockStrategy);
                            packMsgAndSend(MessageId.FORECAST_RECORD_ADD, stockStrategy);
                        }

                        //刷新分析预测结果
                        packMsgAndSend(MessageId.FORECAST_DATA_LIST, stockInfo.getSymbol());
                    }
                }
                break;
            case InitNetInfo.PERIOD_CANDLE_5_MINUTE:
                stockInfo = (StockInfo)data;
                if (stockInfo.getItems().size() > 3) {
                    if (mStockInfos.containsKey(stockInfo.getSymbol())) {
                        mStockInfos.get(stockInfo.getSymbol()).setItems5(stockInfo.getItems());
                        stockInfo = mStockInfos.get(stockInfo.getSymbol());
                    } else {
                        stockInfo.setItems5(stockInfo.getItems());
                        stockInfo.setItems(null);
                        mStockInfos.put(stockInfo.getSymbol(), stockInfo);
                    }

                    long period5 = AnalyzeInd.INSTANCE.analyzeHistIndex(InitAppConstant.MINUTE_5, stockInfo.getServerTime(), stockInfo.getSymbol(), stockInfo.getItems5());
                    mRequestPeriod.get(stockInfo.getSymbol()).m5 = period5;

                    //更新列表名称
                    mStockStrategy.get(stockInfo.getSymbol()).setNameCN(stockInfo.getDetail().getNameCN());
                } else {
                    if (mStockInfos.containsKey(stockInfo.getSymbol())) {
                        long period5 = mRequestPeriod.get(stockInfo.getSymbol()).m5;
                        StockStrategy stockStrategy = mStockStrategy.get(stockInfo.getSymbol());
                        period5 = AnalyzeInd.INSTANCE.analyzeStockInfoResult(InitAppConstant.MINUTE_5, mStockInfos.get(stockInfo.getSymbol()),
                                stockInfo, period5, stockStrategy);
                        mRequestPeriod.get(stockInfo.getSymbol()).m5 = period5;

                        //添加通知和记录
                        if (stockStrategy.isBuy() || stockStrategy.isSell()) {
                            ToolNotification.getInstance().showNotification(stockStrategy);
                            packMsgAndSend(MessageId.FORECAST_RECORD_ADD, stockStrategy);
                        }

                        //刷新分析预测结果
                        packMsgAndSend(MessageId.FORECAST_DATA_LIST, stockInfo.getSymbol());
                    }
                }
                break;
            case InitNetInfo.PERIOD_CANDLE_15_MINUTE:
                stockInfo = (StockInfo)data;
                if (stockInfo.getItems().size() > 3) {
                    if (mStockInfos.containsKey(stockInfo.getSymbol())) {
                        mStockInfos.get(stockInfo.getSymbol()).setItems15(stockInfo.getItems());
                        stockInfo = mStockInfos.get(stockInfo.getSymbol());
                    } else {
                        stockInfo.setItems15(stockInfo.getItems());
                        stockInfo.setItems(null);
                        mStockInfos.put(stockInfo.getSymbol(), stockInfo);
                    }

                    long period15 = AnalyzeInd.INSTANCE.analyzeHistIndex(InitAppConstant.MINUTE_15, stockInfo.getServerTime(), stockInfo.getSymbol(), stockInfo.getItems15());
                    mRequestPeriod.get(stockInfo.getSymbol()).m15 = period15;

                    //更新列表名称
                    mStockStrategy.get(stockInfo.getSymbol()).setNameCN(stockInfo.getDetail().getNameCN());
                } else {
                    if (mStockInfos.containsKey(stockInfo.getSymbol())) {
                        long period15 = mRequestPeriod.get(stockInfo.getSymbol()).m15;
                        StockStrategy stockStrategy = mStockStrategy.get(stockInfo.getSymbol());
                        period15 = AnalyzeInd.INSTANCE.analyzeStockInfoResult(InitAppConstant.MINUTE_15, mStockInfos.get(stockInfo.getSymbol()),
                                stockInfo, period15, stockStrategy);
                        mRequestPeriod.get(stockInfo.getSymbol()).m15 = period15;

                        //添加通知和记录
                        if (stockStrategy.isBuy() || stockStrategy.isSell()) {
                            ToolNotification.getInstance().showNotification(stockStrategy);
                            packMsgAndSend(MessageId.FORECAST_RECORD_ADD, stockStrategy);
                        }

                        //刷新分析预测结果
                        packMsgAndSend(MessageId.FORECAST_DATA_LIST, stockInfo.getSymbol());
                    }
                }
                break;
        }

    }

    @Override
    public void onError(String method, int connCode, String data) {
        packMsgAndSend(MessageId.TOAST_TIP, data);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (super.handleMessage(msg)) {
            return true;
        }

        switch (msg.what) {
            case MessageId.FORECAST_DATA_LIST:
                mIndicatorDetailFra.updateItem(mStockStrategy.get(msg.obj).getPos());
                break;
            case MessageId.FORECAST_DATA_ADD_CODE:
                mIndicatorDetailFra.appendItem(mStockStrategy.get(msg.obj));
                break;
            case MessageId.FORECAST_RECORD_ADD:
                mForecastRecordFra.addHead((StockStrategy) msg.obj);
                break;
            case MessageId.UPDATE_START_STOP:
                updateStartStop();
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            return;
        }

        //设置
        if (1 == requestCode) {
            String[] codes = data.getStringArrayExtra("newCodes");
            String[] removeCodes = data.getStringArrayExtra("removeCodes");

            //请求新的code
            if (null != codes && 0 < codes.length) {
                for (String code : codes) {
                    BaseApplication.mStockCodes.add(code);
                    mRequestPeriod.put(code, new RequestPeriod());
                    ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_1_MINUTE, code, -1, this);
                }
            }


            //存储的数据也清除
            if (null != removeCodes && 0 < removeCodes.length) {
                for (String code : removeCodes) {
                    mRequestPeriod.remove(code);
                    mStockInfos.remove(code);
                    BaseApplication.mStockCodes.remove(code);
                }
            }

            if ((null != codes && 0 < codes.length) || (null != removeCodes && 0 < removeCodes.length)) {
                ToolSharePre.putObject(InitData.TigerStockCodes, BaseApplication.mStockCodes);
            }
        }
    }


    private void updateStartStop() {
        MenuItem menuItem = mMainMenu.findItem(R.id.startOrStop);
        mIsStart = !mIsStart;

        if (mIsStart) {
            if (startService()) {
                menuItem.setTitle("结束");
            } else {
                mIsStart = false;
            }
        } else {
            menuItem.setTitle("开始");
        }
    }

    // TODO: 2017/8/11 请求服务器对照时间，然后定时，股市开市自动请求数据。
    private boolean startService() {
//        if (test()) {
//            return false;
//        }

        if (0 == BaseApplication.mStockCodes.size()) {
            ToolToast.shortToast(this, "目前没有代码可查询");
            return false;
        }

        long curTime = System.currentTimeMillis();
        //关闭状态没有开始都不进行
        if (curTime < ToolTime.mMarketStart || curTime > ToolTime.mMarketEnd) {
            ToolToast.shortToast(this, "股市还没有开始");
            return false;
        }

        mIsStart= true;
        //清空历史数据，重新记录最新的，以免数据不连贯
        mStockInfos = new ArrayMap<>(BaseApplication.mStockCodes.size());
        mRequestPeriod = new ArrayMap<>(BaseApplication.mStockCodes.size());
        for (String symbol : BaseApplication.mStockCodes) {
            mRequestPeriod.put(symbol, new RequestPeriod());    //默认请求时间都是-1
        }

        //请求当前数据
        if (null == mTimer) {
            mTimer = new Timer();
        }
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!mIsStart) {
                    mTimer.cancel();
                    mTimer.purge();
                    mTimer = null;
                    return;
                }

                long curTime = System.currentTimeMillis();
                //获取1分钟数据
                if (false) {
                    for (Map.Entry<String, RequestPeriod> entry : mRequestPeriod.entrySet()) {
                        ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_1_MINUTE, entry.getKey(), entry.getValue().m1, MainActivity.this);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //获取5分钟数据
                if (curTime >= mPeriod5) {
                    for (Map.Entry<String, RequestPeriod> entry : mRequestPeriod.entrySet()) {
                        ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_5_MINUTE, entry.getKey(), entry.getValue().m5, MainActivity.this);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mPeriod5 = curTime + UPDATE_PERIOD;
                }

                //获取15分钟数据
                if (false && curTime >= mPeriod15) {
                    for (Map.Entry<String, RequestPeriod> entry : mRequestPeriod.entrySet()) {
                        ToolRequest.getInstance().getStockInfo(InitNetInfo.PERIOD_CANDLE_15_MINUTE, entry.getKey(), entry.getValue().m15, MainActivity.this);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mPeriod15 = curTime + UPDATE_PERIOD;
                }


            }
        }, 0, mPeriod);

        return true;
    }
}
