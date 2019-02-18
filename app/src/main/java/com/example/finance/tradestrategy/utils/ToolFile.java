package com.example.finance.tradestrategy.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.example.finance.tradestrategy.base.BaseApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by yanghj on 2017/6/4.
 */

public class ToolFile {
    private final static String TAG = "ToolFile";
    private static String mImgPath;
    private static String mFilePath;

    public static String readFileFromAssets(String name) {
        try {
            InputStream fileInputStream = BaseApplication.mContext.getAssets().open(name);
            int lenth = fileInputStream.available();
            byte [] value = new byte[lenth +1];
            fileInputStream.read(value);
            return value.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String readFileFromAssets2(String name) {
        try {
            InputStreamReader inputReader = new InputStreamReader(BaseApplication.mContext.getAssets().open(name) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean removeFile(String pathName) {
        if (TextUtils.isEmpty(pathName)) {
            return false;
        }

        File file = new File(pathName);
        if (file.exists()) {
            file.delete();
            file = null;
        }

        return true;
    }
    /**
     * 保存数据到指定目录file
     * @param path
     * @param data
     * @throws IOException
     */
    public static void saveDirData(String path, String data) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
            file = null;
        }

        FileOutputStream outputStream = new FileOutputStream(path);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        bufferedWriter.write(data, 0, data.length());
        bufferedWriter.close();
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 读取指定目录数据
     * @param path
     * @return
     * @throws IOException
     */
    public static String readDirData(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        FileInputStream inputStream = new FileInputStream(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();

        String line = "";
        while ((line = bufferedReader.readLine()) != null){
            stringBuilder.append(line);
        }

        bufferedReader.close();
        inputStream.close();

        return stringBuilder.toString();
    }

    /**
     * 格式化测试数据文件名
     */
    public static String makeName(String method, String stockCode, long time) {
        return method.replace('/', '_') + "_" + stockCode + "_" + time + ".txt";
    }

    /**
     * 保存数据到app指定目录
     * @param name
     * @param data
     */
    public static void saveAppData(String name, String data) {
        try {
            ToolFile.saveDirData(getFilePath() + name, data);
        } catch (IOException e) {
            e.printStackTrace();
            ToolLog.e(TAG, "saveAppData", e.getMessage());
        }
    }

    /**
     * 保存数据到app指定目录
     * @param name
     */
    public static String readAppData(String name) {
        try {
            return readDirData(getFilePath() + name);
        } catch (IOException e) {
            e.printStackTrace();
            ToolLog.e(TAG, "readAppData", e.getMessage());
        }

        return null;
    }

    public static void initAppDir() {
        String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tradestrategy/";
        mImgPath = basePath + "image/";
        File file = new File(mImgPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        mFilePath = basePath + "file/";
        File fileFile = new File(mFilePath);
        if (!fileFile.exists()) {
            fileFile.mkdirs();
        }

    }

    public static String getImagePath() {
        return mImgPath;
    }

    public static String getFilePath() {
        return mFilePath;
    }

}
