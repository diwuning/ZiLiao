package com.nmpa.nmpaapp.base;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.hikvision.cloud.sdk.CloudOpenSDK;
import com.hikvision.cloud.sdk.core.OnCommonCallBack;
import com.hyphenate.easeui.EaseUI;
import com.igexin.sdk.PushManager;
import com.nmpa.nmpaapp.BuildConfig;
import com.nmpa.nmpaapp.appmanager.LibApp;
import com.nmpa.nmpaapp.constants.ApplicationProperty;
import com.nmpa.nmpaapp.constants.Const;
import com.nmpa.nmpaapp.modules.huanxin.utils.DemoHelper;
import com.nmpa.nmpaapp.service.GePushIntentService;
import com.nmpa.nmpaapp.service.GePushService;
import com.nmpa.nmpaapp.utils.DisplayUtil;
import com.nmpa.nmpaapp.utils.LogUtil;
import com.nmpa.nmpaapp.utils.SPUtils;
import com.nmpa.nmpaapp.utils.SavePreferences;
import com.zhy.autolayout.config.AutoLayoutConifg;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.log.LoggerInterceptor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
public class BaseApplication extends Application {
    private static final String TAG = "BaseApplication";

    private static final String RELEASE = "release";
    private static final String CURRENT_PAKAGENAME = "com.nmpa.nmpaapp";
    String iotToken = "";
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        //初始化内存泄露检测包
        initLeakCanary();
        String curProcessName = getCurProcessName(getApplicationContext());
        //注册ActivityLifecycleCallbacks监听
        ForegroundCallbacks.get(this).addListener(foreGroundSwitchListener);
        if (CURRENT_PAKAGENAME.equals(curProcessName)) {
            //配置AutoLayout库,使用设备的物理高度
            AutoLayoutConifg.getInstance().useDeviceSize().init(getApplicationContext());
            //初始化日志管理
            initLog();
            //初始化网络请求 debug环境下,打印请求
            initOkHttp();
            //初始化commlibrary
            LibApp.init(getApplicationContext());
            //检查进程状态
            checkKillStatus();
            //初始化图片选择器
//            initBoxing();
            //初始化ARouter
            initARouter();
        }
        /**
         * 初始化尺寸工具类
         */
        initDisplayOpinion();
        //个推
        PushManager.getInstance().initialize(getApplicationContext(), GePushService.class);
        PushManager.getInstance().registerPushIntentService(getApplicationContext(), GePushIntentService.class);

        //百度地图
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(getApplicationContext());
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);

        initArgsConfig();
        Log.e(TAG,"token ="+SPUtils.getString(getApplicationContext(),"SP_SDK_OUATH_TOKEN_VAL"));
        if (SPUtils.getString(getApplicationContext(),"SP_SDK_OUATH_TOKEN_VAL") == null || SPUtils.getString(getApplicationContext(),"SP_SDK_OUATH_TOKEN_VAL").equals("")) {
            getIotToken();
        } else {
            iotToken = SPUtils.getString(getApplicationContext(),"SP_SDK_OUATH_TOKEN_VAL");
//            iotToken = "dc892fbc-496a-492c-9987-4cb4b20e1598";
            initIot();
        }
        initHuanxin();
    }
    
    private void initHuanxin() {
        DemoHelper.getInstance().init(this);
//        EMOptions options = new EMOptions();
//        // 默认添加好友时，是不需要验证的，改成需要验证
//        options.setAcceptInvitationAlways(false);
//        // 是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载，如果设为 false，需要开发者自己处理附件消息的上传和下载
////        options.setAutoTransferMessageAttachments(true);
////        // 是否自动下载附件类消息的缩略图等，默认为 true 这里和上边这个参数相关联
////        options.setAutoDownloadThumbnail(true);
//
//        //初始化
//        EMClient.getInstance().init(this, options);
//        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
//        EMClient.getInstance().setDebugMode(true);
        EaseUI.getInstance().init(this, null);
    }

    private void initArgsConfig() {
        String OauthToken = (String) SPUtils.get(this, "SP_SDK_OUATH_TOKEN_VAL", Const.OAUTH_TOKEN);
        String deviceSerial = (String) SPUtils.get(this, "DEVICE_SERIAL", Const.DEVICE_SERIAL);
        String deviceVerifyCode = (String) SPUtils.get(this, "VERIFY_CODE", Const.VERIFY_CODE);
        int deviceChannelNo = (int) SPUtils.get(this, "DEVICE_CHANNEL_NO", 1);
        Const.OAUTH_TOKEN = OauthToken;
        Const.DEVICE_SERIAL = deviceSerial;
        Const.DEVICE_CHANNEL_NO = deviceChannelNo;
        Const.VERIFY_CODE = deviceVerifyCode;
    }

    private void initIot(){
        CloudOpenSDK.getInstance().setLogDebugMode(true) //// 默认日志开关状态：打开，正式发布需要关掉
                //sdk数据缓存加密开关（例如SP存储），放在init()方法前设置
                .setDataCacheEncrypt(true,"DBWRNQ")
                .init(this, iotToken, new OnCommonCallBack() {
//                .init(this, Const.OAUTH_TOKEN, new OnCommonCallBack() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "初始化成功");
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.d(TAG, "初始化失败  "+e.getMessage());
                        if (e.getMessage().equals("认证失败")) {
                            getIotToken();
                        }
                    }
                });
    }

    private void getIotToken() {
        OkHttpUtils.post().url("https://api2.hik-cloud.com/oauth/token")
                .addParams("client_id",Const.CLIENT_ID)
                .addParams("client_secret",Const.CLIENT_SECRET)
                .addParams("grant_type","client_credentials")
                .addParams("scope","app").build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG,"getIotToken e="+e.toString());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG,"getIotToken response="+response.toString());
                        //{"access_token":"136f9a7c-89b2-430c-9f22-eca69d90e4e4","token_type":"bearer","refresh_token":null,"scope":"app","expires_in":258213}
                        try {
                            JSONObject object = new JSONObject(response);
                            SPUtils.put(getApplicationContext(),"SP_SDK_OUATH_TOKEN_VAL",object.getString("access_token"));
                            iotToken = object.getString("access_token");
                            initIot();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    
    private void initARouter() {
        // These two lines must be written before init,
        // otherwise these configurations will be invalid in the init process
        if (BuildConfig.IS_DEBUG) {
            // Print log
            ARouter.openLog();
            // Turn on debugging mode (If you are running in InstantRun mode,
            // you must turn on debug mode! Online version needs to be closed,
            // otherwise there is a security risk)
            ARouter.openDebug();
        }
        // As early as possible, it is recommended to initialize in the Application
        ARouter.init(this);
    }
    
    /**
     * 保存进程id到偏好设置,用于判断进程是否被杀死
     */
    private void checkKillStatus() {
        int pid = android.os.Process.myPid();
        SavePreferences.setData(ApplicationProperty.APP_PID, pid);
    }
    
    /**
     * 获取当前进程名称
     *
     * @param context application对象
     * @return 当前进程名称
     */
    private String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }
    
    /**
     * 初始化日志管理
     */
    private void initLog() {
        if (BuildConfig.IS_DEBUG) {
            LogUtil.setDevelopMode(true);
        } else {
            LogUtil.setDevelopMode(false);
        }
    }
    
    /**
     * 初始化Okhttp
     */
    private void initOkHttp() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);
    }
    
    /**
     * 初始化内存泄露检测包
     */
    private void initLeakCanary() {
        //lekCanary 2.2
        // Content providers are loaded before the application class is created.
        // [LeakSentryInstaller] is used to install [leaksentry.LeakSentry] on application start.
//        if (LeakCanary.isInAnalyzerProcess(getApplicationContext())) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
    }

    private ForegroundCallbacks.Listener foreGroundSwitchListener = new ForegroundCallbacks.Listener() {
        @Override
        public void onBecameForeground() {
            LogUtil.i(TAG, "回到前台");
        }
        @Override
        public void onBecameBackground() {
            LogUtil.i(TAG, "压到后台");
        }
    };

    private void initDisplayOpinion() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenhightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(getApplicationContext(), dm.widthPixels);
        DisplayUtil.screenHightDip = DisplayUtil.px2dip(getApplicationContext(), dm.heightPixels);
    }

}
