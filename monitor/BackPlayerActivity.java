package com.nmpa.nmpaapp.modules.monitor;

import androidx.annotation.Nullable;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.base.BaseActivity;
import com.nmpa.nmpaapp.base.ui.AppBarConfig;
import com.nmpa.nmpaapp.router.Page;
import com.nmpa.nmpaapp.utils.DisplayUtil;
import java.io.File;
import butterknife.BindView;

@Route(path = Page.ACTIVITY_BACK_PLAYER)
public class BackPlayerActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "BackPlayerActivity";
    private Context mContext;
}
