package com.nmpa.nmpaapp.modules.monitor;

import androidx.annotation.Nullable;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.gson.Gson;
import com.hikvision.cloud.sdk.CloudOpenSDK;
import com.hikvision.cloud.sdk.core.CloudVideoPlayer;
import com.hikvision.cloud.sdk.core.OnCommonCallBack;
import com.hikvision.cloud.sdk.core.ptz.PTZAction;
import com.hikvision.cloud.sdk.core.ptz.PTZCommand;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.base.BaseActivity;
import com.nmpa.nmpaapp.base.ui.AppBarConfig;
import com.nmpa.nmpaapp.constants.Const;
import com.nmpa.nmpaapp.router.Page;
import com.nmpa.nmpaapp.utils.DisplayUtil;
import com.nmpa.nmpaapp.utils.RxUtils;
import com.nmpa.nmpaapp.utils.SPUtils;
import com.nmpa.nmpaapp.utils.system.DeviceUtils;
import com.nmpa.nmpaapp.widget.CustomGridView;
import com.nmpa.nmpaapp.widget.TalkView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZVideoQualityInfo;
import com.videogo.widget.CheckTextButton;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import okhttp3.Call;

@Route(path = Page.ACTIVITY_REAL_PLAYER)
public class RealPlayerActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "RealPlayerActivity";
    private Context mContext;
    @BindView(R.id.realplay_id_surface_v)
    SurfaceView mSurfaceView;
    @BindView(R.id.realplay_id_pb)
    ProgressBar mProgressBar;
    @BindView(R.id.realplay_player_area)
    RelativeLayout mPlayerAreaRl;

    @BindView(R.id.play_control_bar)
    LinearLayout mPlayerControlLl;
    @BindView(R.id.player_play_btn)
    RelativeLayout mPlayerPlayLargeBtn;
    @BindView(R.id.play_stop_btn)
    ImageView mPlayerStopBtn;
    @BindView(R.id.play_sound_btn)
    ImageView mPlayerSoundBtn;
    @BindView(R.id.fullscreen_button)
    CheckTextButton mPlayerFullScreenBtn;
    private ScreenOrientationHelper mScreenOrientationHelper = null;// 转屏控制器

    private String mDeviceSerial; // 设备序列号
    private int mChannelNo; // 通道号
    private EZDeviceInfo mDeviceInfo;
    private EZConstants.EZTalkbackCapability mTalkAbility;//设备对讲信息
    private boolean isSupportPTZ; // 是否支持云台操作
    private ArrayList<EZVideoQualityInfo> mVideoQualityList; // 用来存放监控点清晰度的列表
    private int mCurrentlevelQuality = EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel(); // 保存当前的视频码流清晰度
    private CloudVideoPlayer mRealPlayer;

    private boolean isHolderFirstCreated = true;
    private boolean isEncry = false;
    // 视频预览
    @BindView(R.id.tv_play_status)
    TextView mPlayStatusTv;
    @BindView(R.id.realplay_id_play_start_btn)
    Button mPlayStartBtn;
    @BindView(R.id.realplay_id_play_stop_btn)
    Button mPlayStopBtn;
    private boolean isPlayOpenStatus;
    private boolean isOldPlaying; //用于界面不可见和可见切换时，记录是否预览的状态
    
    // 声音开关
    @BindView(R.id.realplay_id_sound_status)
    TextView mSoundStatusTv;
    @BindView(R.id.realplay_id_sound_start_btn)
    Button mSoundStartBtn;
    @BindView(R.id.realplay_id_sound_stop_btn)
    Button mSoundStopBtn;
    private boolean isSoundOpenStatus;
    private AlertDialog mVerifyCodeAlertDialog;

    private Disposable mPlayerDeviceInfoDisposable;
    private RxPermissions mRxPermissions;
    
    // 清晰度切换
    @BindView(R.id.realplay_id_level_status)
    TextView mVideoLevelStatusTv;
    @BindView(R.id.realplay_id_level_flunet_btn)
    Button mVideoLevelFlunetBtn; // 流畅
    @BindView(R.id.realplay_id_level_balanced_btn)
    Button mVideoLevelBalancedBtn; // 均衡
    @BindView(R.id.realplay_id_level_hd_btn)
    Button mVideoLevelHDBtn; // 高清
    @BindView(R.id.realplay_id_level_superclear_btn)
    Button mVideoLevelSuperBtn; // 超清
    private int mVideoLevel;
    //设置清晰度
    private Disposable mPlayerLevelSettingDisposable;

    // 录制功能
    @BindView(R.id.realplay_id_record_status)
    TextView mRecordStatusTv;
    @BindView(R.id.realplay_id_record_start_btn)
    Button mRecordStartBtn;
    @BindView(R.id.realplay_id_record_stop_btn)
    Button mRecordStopBtn;
    private boolean isRecordOpenStatus;
    
    // 对讲功能
    @BindView(R.id.realplay_id_talk_status)
    TextView mTalkStatusTv;
    @BindView(R.id.realplay_id_talk_start_btn)
    Button mTalkStartBtn;
    @BindView(R.id.realplay_id_talk_stop_btn)
    Button mTalkStopBtn;
    private boolean isTalkOpenStatus;
    private PopupWindow mHalfVideoTlakPopupWindow;//半双工对讲
    // 其它功能
    @BindView(R.id.realplay_id_capture_btn)
    Button mCapturePictureBtn; // 拍照
    
    @BindView(R.id.realplay_id_ptz_btn)
    Button mPtzBtn; // 云台控制
    private PopupWindow mCapturePicPopupWindow;
    private ImageView mCaptureImgIv;
    private LinearLayout mPtzControlLy;// 云台控制区中间的轮盘
    private PopupWindow mPtzPopupWindow;// 云台控制窗口

    private List<Button> mLevelBtnList = new ArrayList<>();
    @BindView(R.id.realplay_player_control_area)
    ScrollView mPlayerControlArea;
    @BindView(R.id.realplay_id_record_back_btn)
    Button mRecordBackBtn;
    @BindView(R.id.gv_channel)
    CustomGridView gv_channel;
    @BindView(R.id.tv_channelName)
    TextView tv_channelName;
    
    @Override
    public void onBeforeSetContentView() {
//        StatusBarUtil.translucent(this, ContextCompat.getColor(this, R.color.color_0a5fb6));
    }

    @Override
    public int getLayoutResID() {
        return R.layout.activity_real_player;
    }

    @Override
    protected CharSequence setActionBarTitle() {
        return "视频预览";
    }

    @Nullable
    @Override
    public AppBarConfig getAppBarConfig() {
        return mAppBarCompat;
    }
    
    @Override
    public void initContentView(@Nullable Bundle savedInstanceState) {
        mContext = RealPlayerActivity.this;
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (getIntent().getStringExtra("deviceSerial") != null) {
            mDeviceSerial = getIntent().getStringExtra("deviceSerial");
        } else {
            mDeviceSerial = Const.DEVICE_SERIAL;
        }
        getChannelData();
        mChannelNo = Const.DEVICE_CHANNEL_NO;
        mRxPermissions = new RxPermissions(this);
        mLevelBtnList.add(mVideoLevelFlunetBtn);
        mLevelBtnList.add(mVideoLevelBalancedBtn);
        mLevelBtnList.add(mVideoLevelHDBtn);
        mLevelBtnList.add(mVideoLevelSuperBtn);
        mScreenOrientationHelper = new ScreenOrientationHelper(this, mPlayerFullScreenBtn);
        initView();
        initPlayer();

        gv_channel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mChannelNo = deviceBeans.get(i).getChannelNo();
                tv_channelName.setText(String.valueOf(mChannelNo));
                DeviceBean deviceBean = deviceBeans.get(i);
                if (deviceBean.getIsUse() == 1 && deviceBean.getChannelStatus() == 1) {
                    stopPlay();
                    getDeviceInfo();
                } else {
                    Toast.makeText(mContext,"该通道不在线",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    
    private List<DeviceBean> deviceBeans = new ArrayList<>();
    private void getChannelData() {
        String url = "https://api2.hik-cloud.com/v1/customization/devices/channels/actions/listByDevSerial?deviceSerial="+mDeviceSerial;
        OkHttpUtils.get().url(url).addHeader("Authorization","bearer "+ SPUtils.getString(mContext,"SP_SDK_OUATH_TOKEN_VAL")).build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG,"getChannelData  e="+e);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG,"getChannelData  response="+response);
                        try {
                            JSONObject object = new JSONObject(response);
                            if (object.getInt("code") == 200) {
                                JSONArray array = object.getJSONArray("data");
                                Gson gson = new Gson();
                                for (int i = 0;i<array.length();i++) {
                                    DeviceBean deviceBean = gson.fromJson(array.get(i).toString(), DeviceBean.class);
                                    if (deviceBean.getChannelStatus() == 1 && deviceBean.getIsUse() == 1) {
                                        deviceBeans.add(deviceBean);
                                    }
//                                    deviceBeans.add(deviceBean);
                                }
                                if (deviceBeans != null && deviceBeans.size() != 0) {
                                    mChannelNo = deviceBeans.get(0).getChannelNo();
                                }
                                tv_channelName.setText(String.valueOf(mChannelNo));
                                getDeviceInfo();
                                ChannelAdapter channelAdapter = new ChannelAdapter(mContext,deviceBeans);
                                gv_channel.setAdapter(channelAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    
    private void initView() {
        mSurfaceView.setOnClickListener(this);
        //控制栏
        mPlayerStopBtn.setOnClickListener(this);
        mPlayerSoundBtn.setOnClickListener(this);
        // 视频预览
        mPlayStartBtn.setOnClickListener(this);
        mPlayStopBtn.setOnClickListener(this);
        mPlayerPlayLargeBtn.setOnClickListener(this);
        // 声音开关
        mSoundStartBtn.setOnClickListener(this);
        mSoundStopBtn.setOnClickListener(this);
        // 清晰度切换
        mVideoLevelFlunetBtn.setOnClickListener(this);
        mVideoLevelBalancedBtn.setOnClickListener(this);
        mVideoLevelHDBtn.setOnClickListener(this);
        mVideoLevelSuperBtn.setOnClickListener(this);
        // 录制功能
        mRecordStartBtn.setOnClickListener(this);
        mRecordStopBtn.setOnClickListener(this);
        // 对讲功能
        mTalkStartBtn.setOnClickListener(this);
        mTalkStopBtn.setOnClickListener(this);
        // 其它功能
        mCapturePictureBtn.setOnClickListener(this);
        mPtzBtn.setOnClickListener(this);
        
        View etView = LayoutInflater.from(mContext).inflate(R.layout.item_edit_view, null);
        EditText editText = etView.findViewById(R.id.input_id_et);
        editText.setText(Const.VERIFY_CODE);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("设备验证码")
                .setIcon(R.mipmap.ic_launcher)
                .setView(etView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String verifyCode = editText.getText().toString().trim();
                    if (null != verifyCode && verifyCode.length() > 0) {
                        Const.VERIFY_CODE = verifyCode;
                        SPUtils.put(getApplication(), "VERIFY_CODE", verifyCode);
                        isEncry = true;
                        startPlay(isEncry);
                        mVerifyCodeAlertDialog.dismiss();
                    } else {
                        toast("请输入设备验证码");
                    }
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    mVerifyCodeAlertDialog.dismiss();
                });
        mVerifyCodeAlertDialog = builder.create();
        mVerifyCodeAlertDialog.setCanceledOnTouchOutside(false);

        mRecordBackBtn.setOnClickListener(this);
    }

    private void initPlayer(){
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                // 可见的时候，创建SurfaceView的holder
                // 每次回到该界面，holder都会被重新创建、
                Log.e(TAG,"isHolderFirstCreated="+isHolderFirstCreated+","+isOldPlaying);
                if (isHolderFirstCreated) {
                    isHolderFirstCreated = false;
//                    getDeviceInfo();
                    } else {
                    if (isOldPlaying) {
                        startPlay(isEncry);
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }
            
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                // 不可见的时候  销毁SurfaceView的holder
                // 切到后台界面或返回主界面，holder被销毁了
                isOldPlaying = isPlayOpenStatus;
                Log.e(TAG,"isOldPlaying="+isOldPlaying);
                stopPlay();

            }
        });
    }
    
    private void getDeviceInfo() {
        mPlayerDeviceInfoDisposable = Observable.create((ObservableOnSubscribe<EZDeviceInfo>) emitter -> {
            EZDeviceInfo deviceInfo = CloudOpenSDK.getEZDeviceInfo(mDeviceSerial);
            if (null != deviceInfo) {
                emitter.onNext(deviceInfo);
            } else {
                emitter.onError(new Throwable());
            }
            emitter.onComplete();
        }).compose(RxUtils.io2Main())
                .subscribeWith(new DisposableObserver<EZDeviceInfo>() {

                    @Override
                    public void onNext(EZDeviceInfo deviceInfo) {
                        Log.e(TAG,"DeviceSerial="+deviceInfo.getDeviceSerial());
                        mDeviceInfo = deviceInfo;
                        // 获取对讲信息,对讲模式类型:
                        // 不支持对讲:EZConstants.EZTalkbackCapability.EZTalkbackNoSupport
                        // 支持全双工对讲:EZConstants.EZTalkbackCapability.EZTalkbackFullDuplex
                        // 支持半双工对讲:EZConstants.EZTalkbackCapability.EZTalkbackHalfDuplex
                        mTalkAbility = mDeviceInfo.isSupportTalk();
                        isSupportPTZ = mDeviceInfo.isSupportPTZ();
                        //获取视频清晰度信息
                        List<EZCameraInfo> cameraInfoList = mDeviceInfo.getCameraInfoList();
                        if (null == cameraInfoList) {
                            return;
                        }
                        for (EZCameraInfo cameraInfo : cameraInfoList) {
                            // 先判断通道号
                            if (cameraInfo.getCameraNo() == mChannelNo) {
                                mVideoQualityList = cameraInfo.getVideoQualityInfos();
                                // 设备默认的清晰度为
                                mCurrentlevelQuality = cameraInfo.getVideoLevel().getVideoLevel();
                                String levelName;
                                if (mCurrentlevelQuality == EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel()) {
                                    levelName = "流畅";
                                } else if (mCurrentlevelQuality == EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel()) {
                                    levelName = "均衡";
                                } else if (mCurrentlevelQuality == EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel()) {
                                    levelName = "高清";
                                } else if (mCurrentlevelQuality == EZConstants.EZVideoLevel.VIDEO_LEVEL_SUPERCLEAR.getVideoLevel()) {
                                    levelName = "超清";
                                } else {
                                    levelName = "流畅";
                                }
                                mVideoLevelStatusTv.setText(levelName);
                                setLevelStyle();
                            }
                        }
                        startPlay(isEncry);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof BaseException) {
                            toast(e.getMessage());
                        }
                        if (mPlayerDeviceInfoDisposable != null && !mPlayerDeviceInfoDisposable.isDisposed()) {
                            mPlayerDeviceInfoDisposable.dispose();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mPlayerDeviceInfoDisposable != null && !mPlayerDeviceInfoDisposable.isDisposed()) {
                            mPlayerDeviceInfoDisposable.dispose();
                        }
                    }
                });
    }

    private void setLevelStyle() {
        for (int i=0;i< mVideoQualityList.size();i++) {
            Button button = mLevelBtnList.get(i);
            if (mCurrentlevelQuality == i) {
                setSelectedBtn(button,true);
            } else {
                setSelectedBtn(button,false);
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 横屏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //隐身通知栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

            ViewGroup.LayoutParams rootParams = mPlayerAreaRl.getLayoutParams();
            rootParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            rootParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            rootParams.height = DisplayUtil.getScreenHeight(mContext);
//            rootParams.width = DisplayUtil.getScreenWidth(mContext);
            mPlayerControlArea.setVisibility(View.GONE);

            getSupportActionBar().hide();

            DisplayUtil.hideNavKey(this);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏
            //展示通知栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            ViewGroup.LayoutParams rootParams = mPlayerAreaRl.getLayoutParams();
            rootParams.height = DisplayUtil.dp2px(this, 200);
            rootParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mPlayerControlArea.setVisibility(View.VISIBLE);

            getSupportActionBar().show();
            DisplayUtil.showNavKey(this, 0);
        }
    }

    /*
    *  开始预览
    * */
    private void startPlay(boolean isEncry) {
        mRealPlayer = CloudOpenSDK.getInstance().createPlayer(mDeviceSerial, mChannelNo);
        mRealPlayer.setSurfaceHolder(mSurfaceView.getHolder());
        if (isEncry) {
            mRealPlayer.setPlayVerifyCode(Const.VERIFY_CODE);
        }
        mRealPlayer.startRealPlay();
        mPlayerPlayLargeBtn.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mRealPlayer.setOnRealPlayListener(new CloudVideoPlayer.OnRealPlayListener() {
            @Override
            public void onVideoSizeChanged(int videoWidth, int videoHeight) {

            }

            @Override
            public void onRealPlaySuccess() {
                mScreenOrientationHelper.enableSensorOrientation();
                isPlayOpenStatus = true;
                mProgressBar.setVisibility(View.GONE);
                mPlayerPlayLargeBtn.setVisibility(View.GONE);
                mPlayStatusTv.setText("开启");
                setSelectedBtn(mPlayStartBtn,true);
                setSelectedBtn(mPlayStopBtn,false);
                // 默认开启声音
                if (mRealPlayer.openSound()) {
                    isSoundOpenStatus = true;
                    mSoundStatusTv.setText("开启");
                    setSelectedBtn(mSoundStartBtn,true);
                    setSelectedBtn(mSoundStopBtn,false);
                }
                mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_selector);
                mPlayerStopBtn.setBackgroundResource(R.drawable.player_stop_selector);
            }

            @Override
            public void onStopRealPlaySuccess() {
                isPlayOpenStatus = false;
                isSoundOpenStatus = false;
                mPlayStatusTv.setText("关闭");
                setSelectedBtn(mPlayStartBtn,false);
                setSelectedBtn(mPlayStopBtn,true);
                mSoundStatusTv.setText("关闭");
                setSelectedBtn(mSoundStartBtn,false);
                setSelectedBtn(mSoundStopBtn,true);
                mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_off_selector);
                mPlayerStopBtn.setBackgroundResource(R.drawable.player_play_selector);
                mPlayerControlLl.setVisibility(View.GONE);
                mPlayerPlayLargeBtn.setVisibility(View.VISIBLE);
            }

            /**
             * 播放失败回调,得到失败信息
             *
             * @param errorCode   播放失败错误码
             * @param moduleCode  播放失败模块错误码
             * @param description 播放失败描述
             * @param sulution    播放失败解决方方案
             */
            @Override
            public void onRealPlayFailed(int errorCode, String moduleCode, String description, String sulution) {
//                toast(String.format("errorCode：%d, %s", errorCode, description));
                if (description.contains("设备不在线")) {
                    toast(String.format("%s", "设备不在线"));
                } else if (errorCode == 400036){
                    toast(String.format("%s", "视频密码错误"));
                } else {
                    toast(String.format("%s", description));
                }

                Log.d(TAG,"errorCode="+errorCode+",description="+description+","+moduleCode+","+sulution);
                isPlayOpenStatus = false;
                isSoundOpenStatus = false;
                mProgressBar.setVisibility(View.GONE);

                if (errorCode == 400035 || errorCode == 400036) {
                    //
                    //回调时查看errorCode，如果为400035（需要输入验证码）和400036（验证码错误），
                    // 则需要开发者自己处理让用户重新输入验证密码，并调用setPlayVerifyCode设置密码，
                    // 然后重新启动播放
                    // TODO
                    if (!mVerifyCodeAlertDialog.isShowing()) {
                        mVerifyCodeAlertDialog.show();
                    }
                    return;
                }
                
                stopPlay();
                mPlayStatusTv.setText("关闭");
            }
        });
    }

    private void stopPlay() {
        //mScreenOrientationHelper.disableSensorOrientation();
        if (null != mRealPlayer) {
            mRealPlayer.closeSound();
            mRealPlayer.stopRealPlay(); // 停止播放
        }
    }
    
    private boolean containVideoLevel(int level) {
        if (null == mVideoQualityList) {
            return false;
        }
        for (EZVideoQualityInfo qualityInfo : mVideoQualityList) {
            if (level == qualityInfo.getVideoLevel()) {
                return true;
            }
        }
        return false;
    }
    
    /*
     *  设置清晰度
     * */
    private void setVideoLevel(final String levelName, final int levelVal) {
        if (mCurrentlevelQuality == levelVal) {
            Toast.makeText(mContext, String.format("当前清晰度已是%s，请勿重复操作", levelName), Toast.LENGTH_SHORT).show();
            return;
        }
        
        mPlayerLevelSettingDisposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            // 设置清晰度接口为耗时操作，必须在子线程中调用
            boolean isSuccess = mRealPlayer.setVideoLevel(levelVal);
            emitter.onNext(isSuccess);
            emitter.onComplete();
        }).compose(RxUtils.io2Main())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean isSuccess) {
                        String toastMsg;
                        Log.e(TAG,"mCurrentlevelQuality="+levelVal);
                        if (isSuccess) {
                            mCurrentlevelQuality = levelVal;
                            toastMsg = String.format("设置清晰度(%s)成功", levelName);
                            mVideoLevelStatusTv.setText(levelName);
                            setLevelStyle();
                            // 视频播放成功后设置了清晰度需要先停止播放stopRealPlay然后重新开启播放startRealPlay才能生效
                            stopPlay();
                            startPlay(isEncry);
                        } else {
                            toastMsg = String.format("设置清晰度(%s)失败", levelName);
                        }
                        Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT).show();
                    }
                    
                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerLevelSettingDisposable != null && !mPlayerLevelSettingDisposable.isDisposed()) {
                            mPlayerLevelSettingDisposable.dispose();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mPlayerLevelSettingDisposable != null && !mPlayerLevelSettingDisposable.isDisposed()) {
                            mPlayerLevelSettingDisposable.dispose();
                        }
                    }
                });
    }

    /*
     *  对讲监听回调
     * */
    private CloudVideoPlayer.OnVoiceTalkListener onVoiceTalkListener = new CloudVideoPlayer.OnVoiceTalkListener() {
        @Override
        public void onStartVoiceTalkSuccess() {
            mTalkStatusTv.setText("开启");
            setSelectedBtn(mTalkStartBtn,true);
            setSelectedBtn(mTalkStopBtn, false);
            // 如果为半双工的情况下，设置pressed状态
            if (mTalkAbility == EZConstants.EZTalkbackCapability.EZTalkbackHalfDuplex) {
                mRealPlayer.setVoiceTalkStatus(true);
            }
            // TODO
        }
        
        @Override
        public void onStopVoiceTalkSuccess() {
            // 停止对讲成功
        }

        /*
         * 对讲失败回调,得到失败信息
         * @param errorCode   播放失败错误码
         * @param description 播放失败描述
         * @param sulution    播放失败解决方方案
         * */
        @Override
        public void onVoiceTalkFail(int errorCode, String moduleCode, String description, String sulution) {
            //开启对讲失败或停止对讲失败，这里需要开发者自己去判断是开启操作还是停止的操作
            //停止对讲失败后，不影响下一次的start使用
            // TOTO
            toast(description);
        }
    };
    
    /*
     *  打开半双工按钮窗口
     * */
    private void showHalfVideoTalkPopupWindow() {
        hideHalfVideoTalkPopupWindow();

        if (null == mHalfVideoTlakPopupWindow) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.pop_half_video_talk, null, true);
            ((TalkView) layoutView.findViewById(R.id.half_talk_btn)).setOnHalfTalkLsn(onHalfTalkTouchListener);
            int height = DeviceUtils.getScreenHeight(this) - (getSupportActionBar().getHeight() + mPlayerAreaRl.getHeight());
            mHalfVideoTlakPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.MATCH_PARENT, height, true);
            mHalfVideoTlakPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mHalfVideoTlakPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
            mHalfVideoTlakPopupWindow.setFocusable(true);
            mHalfVideoTlakPopupWindow.setOutsideTouchable(false);
            mHalfVideoTlakPopupWindow.showAsDropDown(mPlayerAreaRl);
            mHalfVideoTlakPopupWindow.setOnDismissListener(() -> {
                hideHalfVideoTalkPopupWindow();
            });
        }
        mHalfVideoTlakPopupWindow.update();
    }

    private void hideHalfVideoTalkPopupWindow() {
        if (mHalfVideoTlakPopupWindow != null) {
            mHalfVideoTlakPopupWindow.dismiss();
            mHalfVideoTlakPopupWindow = null;
        }
    }
    
    //半双工说话按钮监听回调
    private TalkView.OnHalfTalkLsn onHalfTalkTouchListener = new TalkView.OnHalfTalkLsn() {
        @Override
        public void onDown() {
            mRealPlayer.setOnVoicTalkListener(onVoiceTalkListener);
            mRealPlayer.startVoiceTalk();
            // 等startVoiceTalk成功后setVoiceTalkStatus(true)
        }
        
        @Override
        public void onUp() {
            mRealPlayer.stopVoiceTalk();
            mRealPlayer.setVoiceTalkStatus(false);
            mTalkStatusTv.setText("关闭");
        }

        @Override
        public void close() {
            hideHalfVideoTalkPopupWindow();
        }
    };
    
    /*
     *  打开抓图图片显示窗口
     * */
    private void showCapturePicPopupWindow(Bitmap bitmap) {
        hideCapturePicPopupWindow();

        if (null == mCapturePicPopupWindow) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.dialog_capture_pic, null, true);
            ImageView closeBtn = layoutView.findViewById(R.id.capture_close_ic);
            mCaptureImgIv = layoutView.findViewById(R.id.capture_img);
            mCapturePicPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, true);
            mCapturePicPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mCapturePicPopupWindow.setAnimationStyle(R.style.popwindowAppearAnim);
            mCapturePicPopupWindow.setFocusable(true);
            mCapturePicPopupWindow.setOutsideTouchable(false);
            mCapturePicPopupWindow.showAsDropDown(mPlayerAreaRl);
            mCapturePicPopupWindow.setOnDismissListener(() -> {
                hideCapturePicPopupWindow();
            });
            closeBtn.setOnClickListener(v -> {
                hideCapturePicPopupWindow();
            });
        }
        mCaptureImgIv.setImageBitmap(bitmap);
        mCapturePicPopupWindow.update();
    }

    private void hideCapturePicPopupWindow() {
        if (mCapturePicPopupWindow != null) {
            mCapturePicPopupWindow.dismiss();
            mCapturePicPopupWindow = null;
        }
    }
    
    /*
     *  打开云台控制窗口
     * */
    private void showPtzPopupWindow() {
        hidePtzPopupWindow();

        if (null == mPtzPopupWindow) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.video_ptz_control, null, true);
            mPtzControlLy = layoutView.findViewById(R.id.ptz_control_ly);
            layoutView.findViewById(R.id.ptz_close_btn).setOnClickListener(v -> hidePtzPopupWindow());
            layoutView.findViewById(R.id.ptz_top_btn).setOnTouchListener(cloudTouchListener);
            layoutView.findViewById(R.id.ptz_bottom_btn).setOnTouchListener(cloudTouchListener);
            layoutView.findViewById(R.id.ptz_left_btn).setOnTouchListener(cloudTouchListener);
            layoutView.findViewById(R.id.ptz_right_btn).setOnTouchListener(cloudTouchListener);
            layoutView.findViewById(R.id.btnBigger).setOnTouchListener(touchListener);
            layoutView.findViewById(R.id.btnSmaller).setOnTouchListener(touchListener);
            int height = DeviceUtils.getScreenHeight(this) - (getSupportActionBar().getHeight() + mPlayerAreaRl.getHeight());
            mPtzPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.MATCH_PARENT, height, true);
            mPtzPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPtzPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
            mPtzPopupWindow.setFocusable(true);
            mPtzPopupWindow.setOutsideTouchable(false);
            mPtzPopupWindow.showAsDropDown(mPlayerAreaRl);
            mPtzPopupWindow.setOnDismissListener(() -> {
                hidePtzPopupWindow();
            });
        }
        mPtzPopupWindow.update();
    }

    private void hidePtzPopupWindow() {
        if (mPtzPopupWindow != null) {
            mPtzPopupWindow.dismiss();
            mPtzPopupWindow = null;
            mPtzControlLy = null;
        }
    }
    
    /*
     *  云台操作监听回调:放大/缩小
     * */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // 手指按下
            PTZCommand ptzCommand = null;
            PTZAction ptzAction = null;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setBackgroundResource(R.drawable.menu_seleted_shape);
                switch (v.getId()) {
                    case R.id.btnBigger:
                        ptzCommand = PTZCommand.ZOOMIN;
                        break;
                    case R.id.btnSmaller:
                        ptzCommand = PTZCommand.ZOOMOUT;
                        break;
                    default:
                        break;
                }
                ptzAction = PTZAction.START;
            }
            // 手指抬起
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.setBackgroundResource(R.drawable.menu_normal_shape);
                switch (v.getId()) {
                    case R.id.btnBigger:
                        ptzCommand = PTZCommand.ZOOMIN;
                        break;
                    case R.id.btnSmaller:
                        ptzCommand = PTZCommand.ZOOMOUT;
                        break;
                    default:
                        break;
                }
                ptzAction = PTZAction.STOP;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_UP) {
                invokePTZControl(ptzCommand, ptzAction);
            }
            return true;
        }
    };

    //执行云台指令
    private void invokePTZControl(final PTZCommand ptzCommand,
                                  final PTZAction ptzAction) {
        if (null == ptzCommand || null == ptzAction) {
            return;
        }
        final int speed = 2;// 0-2，默认为2
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            // 必须在子线程中调用
            CloudOpenSDK.getInstance().controlPTZ(mDeviceSerial,
                    mChannelNo,
                    ptzCommand,
                    ptzAction,
                    speed
                    , new OnCommonCallBack() {
                        @Override
                        public void onSuccess() {
                            emitter.onNext(true);
                        }
                        @Override
                        public void onFailed(Exception e) {
                            emitter.onError(e);
                        }
                    });
        }).compose(RxUtils.io2Main())
                .subscribe(new Observer<Boolean>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }
                    
                    @Override
                    public void onNext(Boolean value) {
                        if (null != disposable && !disposable.isDisposed()) {
                            disposable.dispose();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        toast(e.getMessage());
                        if (null != disposable && !disposable.isDisposed()) {
                            disposable.dispose();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    
    /*
     *  云台操作上下左右监听回调
     * */
    private View.OnTouchListener cloudTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionevent) {
            int action = motionevent.getAction();
            PTZCommand ptzCommand = null;
            PTZAction ptzAction = null;
            int backgroundResourceId = -1;
            if (action == MotionEvent.ACTION_DOWN) {
                switch (view.getId()) {
                    case R.id.ptz_top_btn:
                        // 上
                        backgroundResourceId = R.mipmap.ptz_up_sel;
                        ptzCommand = PTZCommand.UP;
                        break;
                    case R.id.ptz_bottom_btn:
                        // 下
                        backgroundResourceId = R.mipmap.ptz_bottom_sel;
                        ptzCommand = PTZCommand.DOWN;
                        break;
                    case R.id.ptz_left_btn:
                        // 左
                        backgroundResourceId = R.mipmap.ptz_left_sel;
                        ptzCommand = PTZCommand.LEFT;
                        break;
                    case R.id.ptz_right_btn:
                        // 右
                        backgroundResourceId = R.mipmap.ptz_right_sel;
                        ptzCommand = PTZCommand.RIGHT;
                        break;
                    default:
                        break;
                }
                ptzAction = PTZAction.START;
            } else if (action == MotionEvent.ACTION_UP) {
                switch (view.getId()) {
                    case R.id.ptz_top_btn:
                        ptzCommand = PTZCommand.UP;
                        break;
                    case R.id.ptz_bottom_btn:
                        ptzCommand = PTZCommand.DOWN;
                        break;
                    case R.id.ptz_left_btn:
                        ptzCommand = PTZCommand.LEFT;
                        break;
                    case R.id.ptz_right_btn:
                        ptzCommand = PTZCommand.RIGHT;
                        break;
                    default:
                        break;
                }
                backgroundResourceId = R.mipmap.ptz_bg;
                ptzAction = PTZAction.STOP;
            }
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
                mPtzControlLy.setBackgroundResource(backgroundResourceId);
                invokePTZControl(ptzCommand, ptzAction);
            }
            return true;
        }
    };

    private void setSelectedBtn(Button btn, boolean isSeled){
        if (isSeled) {
            btn.setTextColor(getResources().getColor(R.color.realplayer_blue));
            btn.setSelected(true);
        } else {
            btn.setTextColor(getResources().getColor(R.color.color_333333));
            btn.setSelected(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.player_play_btn:
                startPlay(isEncry);
                toast("开始预览");
                break;
            case R.id.realplay_id_surface_v:
                if (!isPlayOpenStatus) {
                    return;
                }
                if (mPlayerControlLl.getVisibility() == View.VISIBLE) {
                    mPlayerControlLl.setVisibility(View.GONE);
                } else {
                    mPlayerControlLl.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.play_stop_btn:
                if (isPlayOpenStatus) {
                    // TODO 关闭对讲，云台操作
                    stopPlay();
                    isPlayOpenStatus = false;
                } else {
                    startPlay(isEncry);
                    isPlayOpenStatus = true;
                }
                break;
            case R.id.play_sound_btn:
                String toastMsg;
                if (!isPlayOpenStatus) {
                    return;
                }
                boolean isSuccess;
                if (isSoundOpenStatus) {
                    isSuccess = mRealPlayer.closeSound();
                    if (isSuccess) {
                        isSoundOpenStatus = false;
                        toastMsg = "声音关闭成功";
                        mSoundStatusTv.setText("关闭");
                        setSelectedBtn(mSoundStartBtn,false);
                        setSelectedBtn(mSoundStopBtn, true);
                        mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_off_selector);
                    } else {
                        toastMsg = "声音关闭失败";
                    }
                } else {
                    isSuccess = mRealPlayer.openSound();
                    if (isSuccess) {
                        isSoundOpenStatus = true;
                        toastMsg = "声音开启成功";
                        mSoundStatusTv.setText("开启");
                        setSelectedBtn(mSoundStartBtn,true);
                        setSelectedBtn(mSoundStopBtn, false);
                        mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_selector);
                    } else {
                        toastMsg = "声音开启失败";
                    }
                }
                if (null != toastMsg) {
                    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.realplay_id_play_start_btn:
                if (!isPlayOpenStatus) {
                    startPlay(isEncry);
                    toast("开始预览");
                } else {
                    toast("预览已开启，请勿重复操作");
                }
                break;
            case R.id.realplay_id_play_stop_btn:
                if (isPlayOpenStatus) {
                    stopPlay();
                    toast(" 停止预览");
                } else {
                    toast("预览已关闭，请勿重复操作");
                }
                break;
            // 声音开关
            case R.id.realplay_id_sound_start_btn:
                if (!isSoundOpenStatus) {
                    if (mRealPlayer.openSound()) {
                        isSoundOpenStatus = true;
                        toast("声音开启成功");
                        mSoundStatusTv.setText("开启");
                        setSelectedBtn(mSoundStartBtn,true);
                        setSelectedBtn(mSoundStopBtn, false);
                        mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_selector);
                    } else {
                        toast("声音开启失败");
                    }
                } else {
                    toast("声音已开启，请勿重复操作");
                }
                break;
            case R.id.realplay_id_sound_stop_btn:
                if (isSoundOpenStatus) {
                    if (mRealPlayer.closeSound()) {
                        isSoundOpenStatus = false;
                        toast("声音关闭成功");
                        mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_off_selector);
                        mSoundStatusTv.setText("关闭");
                        setSelectedBtn(mSoundStartBtn,false);
                        setSelectedBtn(mSoundStopBtn, true);
                    } else {
                        toast("声音关闭失败");
                    }
                } else {
                    toast("声音已关闭，请勿重复操作");
                }
                break;
            // 清晰度切换
            case R.id.realplay_id_level_flunet_btn:
                if (!containVideoLevel(EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel())) {
                    toast("该设备不支持切流畅清晰度");
                } else {
                    setVideoLevel("流畅", EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel());
                }
                break;
            case R.id.realplay_id_level_balanced_btn:
                if (!containVideoLevel(EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel())) {
                    toast("该设备不支持切均衡清晰度");
                } else {
                    setVideoLevel("均衡", EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel());
                }
                break;
            case R.id.realplay_id_level_hd_btn:
                if (!containVideoLevel(EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel())) {
                    toast("该设备不支持切高清清晰度");
                } else {
                    setVideoLevel("高清", EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel());
                }
                break;
            case R.id.realplay_id_level_superclear_btn:
                if (!containVideoLevel(EZConstants.EZVideoLevel.VIDEO_LEVEL_SUPERCLEAR.getVideoLevel())) {
                    toast("该设备不支持切超清清晰度");
                } else {
                    setVideoLevel("超清", EZConstants.EZVideoLevel.VIDEO_LEVEL_SUPERCLEAR.getVideoLevel());
                }
                break;
            // 录制功能
            case R.id.realplay_id_record_start_btn:
                // 获取权限逻辑
                mRxPermissions.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(permission -> {
                            if (permission.granted) {
                                if (!isRecordOpenStatus) {
                                    // 录制本地路径,例如：Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath())：+"/hikvision/123.mp4"
                                    final String fileName = String.format("hik_%s.mp4", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                                    final String recordPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/" + fileName;
                                    boolean recordStartSuccess = mRealPlayer.startLocalRecordWithFile(recordPath);
                                    if (recordStartSuccess) {
                                        isRecordOpenStatus = true;
                                        toast("开启成功，开始录制");
                                        mRecordStatusTv.setText("开启");
                                        setSelectedBtn(mRecordStartBtn,true);
                                        setSelectedBtn(mRecordStopBtn, false);
                                    } else {
                                        toast("录制开启失败");
                                    }
                                } else {
                                    toast("录制已开启，请勿重复操作");
                                }
                            } else if (permission.shouldShowRequestPermissionRationale) {
                                toast("录制开启失败，拒绝权限，等待下次询问哦");
                            } else {
                                toast("录制开启失败，不再弹出询问框，请前往APP应用设置中打开此权限");
                            }
                        });
                break;
            case R.id.realplay_id_record_stop_btn:
                if (isRecordOpenStatus) {
                    boolean recordStopSuccess = mRealPlayer.stopLocalRecord();
                    if (recordStopSuccess) {
                        isRecordOpenStatus = false;
                        toast("录制关闭成功");
                        mRecordStatusTv.setText("关闭");
                        setSelectedBtn(mRecordStartBtn,false);
                        setSelectedBtn(mRecordStopBtn, true);
                    } else {
                        toast("录制关闭成功");
                    }
                } else {
                    toast("录制已关闭，请勿重复操作");
                }
                break;
            case R.id.realplay_id_talk_start_btn:
                // 获取权限
                mRxPermissions.requestEach(Manifest.permission.RECORD_AUDIO)
                        .subscribe(permission -> {
                            if (permission.granted) {
                                Log.e(TAG,"mTalkAbility="+mTalkAbility);
                                if (mTalkAbility == EZConstants.EZTalkbackCapability.EZTalkbackFullDuplex) {
                                    mRealPlayer.setOnVoicTalkListener(onVoiceTalkListener);
                                    mRealPlayer.startVoiceTalk();
                                } else if (mTalkAbility == EZConstants.EZTalkbackCapability.EZTalkbackHalfDuplex) {
                                    showHalfVideoTalkPopupWindow();
                                } else {
                                    toast("该设备不支持对讲功能");
                                }
                            } else if (permission.shouldShowRequestPermissionRationale) {
                                toast("对讲开启失败，拒绝权限，等待下次询问哦");
                            } else {
                                toast("对讲开启失败，不再弹出询问框，请前往APP应用设置中打开此权限");
                            }
                        });

                break;
            case R.id.realplay_id_talk_stop_btn:
                mRealPlayer.stopVoiceTalk();
                mTalkStatusTv.setText("关闭");
                break;
            // 其它功能
            case R.id.realplay_id_capture_btn:
                if (!isPlayOpenStatus) {
                    return;
                }
                Bitmap captureBitmap = mRealPlayer.capturePicture(); //图片很大的情况下，建议先写到本地，再压缩读出来渲染到界面上
                showCapturePicPopupWindow(captureBitmap);
                break;
            case R.id.realplay_id_ptz_btn:
                if (isSupportPTZ) {
                    showPtzPopupWindow();
                } else {
                    toast("该设备不支持云台操作");
                }
                break;
            case R.id.realplay_id_record_back_btn:
                Intent backIntent = new Intent(mContext,BackPlayerListActivity.class);
                startActivity(backIntent);
                break;
            default:
                break;
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        mScreenOrientationHelper.postOnStart();
        mScreenOrientationHelper.disableSensorOrientation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScreenOrientationHelper.postOnStop();
    }
    
    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mScreenOrientationHelper.portrait();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRealPlayer) {
            mRealPlayer.release();
        }
        if (null != mPtzPopupWindow && mPtzPopupWindow.isShowing()) {
            mPtzPopupWindow.dismiss();
        }
        
        if (null != mHalfVideoTlakPopupWindow && mHalfVideoTlakPopupWindow.isShowing()) {
            mHalfVideoTlakPopupWindow.dismiss();
        }
    }
}
