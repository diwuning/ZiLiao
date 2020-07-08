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
}
