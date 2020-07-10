package com.hongyang.firefapp.utils;

import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.hongyang.firefapp.base.MyApplication;
import com.hongyang.firefapp.bean.InfoAlertBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AssetsUtils {
    public static Object assetsFile2Bean(String assetsName, Class beanClass) {
        //将json数据变成字符串
        StringBuilder result = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = MyApplication.getInstance().getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(assetsName)));
            String line;
            while ((line = bf.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (!result.toString().equals("")) {
            return new Gson().fromJson(result.toString(), beanClass);
        } else {
            return null;
        }
    }
  
    public static Object assetsFile2Data(String assetsName) {
        //将json数据变成字符串
        StringBuilder result = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = MyApplication.getInstance().getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(assetsName)));
            String line;
            while ((line = bf.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (!result.toString().equals("")) {
            return new Gson().fromJson(result.toString(), InfoAlertBean.class);
        } else {
            return null;
        }
    }

}
