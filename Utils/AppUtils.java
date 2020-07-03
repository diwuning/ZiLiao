package com.nmpa.nmpaapp.appmanager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.nmpa.nmpaapp.utils.LogUtil;
import java.io.File;
public class AppUtils {

    private static final String TAG = "AppUtils";
    /**
     * 获取应用的版本名称
     *
     * @return 版本名称VersionName
     */
    public static String getAppVersionName() {
        PackageManager packageManager = LibApp.getContext().getPackageManager();
        PackageInfo packageInfo;
        try {
          packageInfo = packageManager.getPackageInfo(LibApp.getContext().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用的版本号
     *
     * @return 版本号VersionCode
     */
     public static int getAppVersionCode() {
        PackageManager packageManager = LibApp.getContext().getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(LibApp.getContext().getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * 安装一个apk文件
     *
     * @param context 上下文
     * @param uriFile apk文件
     */
    public static void install(Context context, File uriFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(uriFile), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    /**
     * 读取本地apk的信息
     *
     * @param apkFilePath 安装包路径
     * @return 包信息
     */
    private PackageInfo getApkInfo(String apkFilePath) {
        PackageManager pm = LibApp.getContext().getPackageManager();
        return pm.getPackageArchiveInfo(apkFilePath, PackageManager.GET_ACTIVITIES);
    }
    
    /**
     * 判断是否在主进程
     *
     * @param context 上下文
     * @return 是否在主进程
     */
    public static boolean inMainProcess(Context context) {
        String packageName = context.getPackageName();
        String processName = getProcessName(context);
        LogUtil.i(TAG, "packageName: " + packageName);
        LogUtil.i(TAG, "inMainProcess: " + processName);
        return packageName.equals(processName);
    }
    
    /**
     * 获取当前进程名
     *
     * @param context 上下文
     * @return 进程名
     */
    public static String getProcessName(Context context) {

        String processName = null;
        // ActivityManager
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        while (true) {
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info.pid == android.os.Process.myPid()) {
                    processName = info.processName;
                    break;
                }
            }

            // go home
            if (!TextUtils.isEmpty(processName)) {
                return processName;
            }
            // take a rest and again
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * 获取本应用的包名
     *
     * @param context 上下文
     * @return 返回包名
     */
     public static String getPackageName(Context context) {
        String packageName = null;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            packageName = pi.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageName;
    }
}
