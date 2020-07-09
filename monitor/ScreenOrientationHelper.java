package com.nmpa.nmpaapp.modules.monitor;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.View.OnClickListener;
import com.videogo.widget.CheckTextButton;

/**
 * 屏幕旋转控制器
 */
public class ScreenOrientationHelper implements SensorEventListener {

    private Activity mActivity;// 需要被控制的Activity
    private CheckTextButton mButton1, mButton2;// 竖屏/横屏按钮
    private int mOriginOrientation;// 屏幕方向
    private Boolean mPortraitOrLandscape;// 竖屏=true，横屏=false
}
