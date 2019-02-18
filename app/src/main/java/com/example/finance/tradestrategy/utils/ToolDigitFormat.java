package com.example.finance.tradestrategy.utils;

import java.math.BigDecimal;

/**
 * Created by yanghj on 2017/6/3.
 */

public class ToolDigitFormat {

    //默认两位
    public static String floatToStr(float value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    public static float decimalPlace(float value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }
}
