package com.example.finance.tradestrategy.base;

import android.app.Application;
import android.content.Context;
import android.support.v4.util.ArraySet;

import com.example.finance.tradestrategy.globaldata.InitData;
import com.example.finance.tradestrategy.utils.ToolFile;

import java.util.Set;

/**
 * Created by yanghj on 2017/6/4.
 */

public class BaseApplication extends Application {

    public static Context  mContext;


    public static boolean       mIsLogined = false;

    //监控的stocks
    public static Set<String> mStockCodes = new ArraySet<>(5);

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();

        initData();
    }



    private void initData() {
        ToolFile.initAppDir();
//        mStockCodes = ToolSharePre.getObjectSet(InitData.TigerStockCodes, String.class);
//        mStockCodes.add(InitData.Stock_DUST);
//        mStockCodes.add(InitData.Stock_NUGT);
//        mStockCodes.add(InitData.Stock_JNUG);
//        mStockCodes.add(InitData.Stock_UGAZ);
//        mStockCodes.add(InitData.Stock_DGAZ);
//        mStockCodes.add(InitData.Stock_JDST);
//        mStockCodes.add(InitData.Stock_MOMO);
//        mStockCodes.add(InitData.Stock_DWT);
//        mStockCodes.add(InitData.Stock_UWT);
//        mStockCodes.add(InitData.Stock_NVDA);
        mStockCodes.add(InitData.Stock_X);
        mStockCodes.add(InitData.Stock_BABA);
        mStockCodes.add(InitData.Stock_JD);
        mStockCodes.add(InitData.Stock_AMD);
        mStockCodes.add(InitData.Stock_UVXY);
//        ToolLog.i("读取数据完成-----排行榜：" + BaseApplication.mRankLongPeriod.size() + "  images:" + BaseApplication.mSavedUserImgs.size());
    }
}
