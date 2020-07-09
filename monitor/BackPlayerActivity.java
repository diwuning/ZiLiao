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
    @BindView(R.id.rl_surface)
    RelativeLayout rl_surface;
    @BindView(R.id.sv)
    SurfaceView sv;
    @BindView(R.id.seekBar)
    SeekBar seekBar;
    @BindView(R.id.iv_play)
    ImageView iv_play;
    @BindView(R.id.iv_pause)
    ImageView iv_pause;
    private String path = "";
    private MediaPlayer mediaPlayer;
    private int currentPosition = 0;
    private boolean isPlaying;

    @Override
    public void onBeforeSetContentView() {

    }

    @Override
    public int getLayoutResID() {
        return R.layout.activity_back_player;
    }
    
    @Override
    protected CharSequence setActionBarTitle() {
        return "视频回看";
    }

    @Nullable
    @Override
    public AppBarConfig getAppBarConfig() {
        return mAppBarCompat;
    }

    @Override
    public void initContentView(@Nullable Bundle savedInstanceState) {
        mContext = BackPlayerActivity.this;
        iv_play.setOnClickListener(this);
        sv.setOnClickListener(this);
        iv_pause.setOnClickListener(this);
        sv.getHolder().addCallback(callback);
        seekBar.setOnSeekBarChangeListener(change);
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (getIntent().getStringExtra("path") != null) {
            path = getIntent().getStringExtra("path");
            Log.i(TAG, "path="+path);
        }
    }
    
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        // SurfaceHolder被修改的时候回调
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "SurfaceHolder 被销毁");
            // 销毁SurfaceHolder的时候记录当前的播放位置并停止播放
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
            }
        }
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "SurfaceHolder 被创建");
//            if (currentPosition > 0) {
                // 创建SurfaceHolder的时候，如果存在上次播放的位置，则按照上次播放位置进行播放
                play(currentPosition);
//                currentPosition = 0;
//            }
        }
        
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Log.i(TAG, "SurfaceHolder 大小被改变");
        }

    };

    private SeekBar.OnSeekBarChangeListener change = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // 当进度条停止修改的时候触发
            // 取得当前进度条的刻度
            int progress = seekBar.getProgress();
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                // 设置当前播放的位置
                mediaPlayer.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 横屏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //隐身通知栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

            ViewGroup.LayoutParams rootParams = rl_surface.getLayoutParams();
            rootParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            rootParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

            getSupportActionBar().hide();

            DisplayUtil.hideNavKey(this);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏
            //展示通知栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            ViewGroup.LayoutParams rootParams = rl_surface.getLayoutParams();
            rootParams.height = DisplayUtil.dp2px(this, 200);
            rootParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            mPlayerControlArea.setVisibility(View.VISIBLE);

            getSupportActionBar().show();
            DisplayUtil.showNavKey(this, 0);
        }
    }

    protected void play(final int msec) {
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(this, "视频文件路径错误", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 设置播放的视频源
            mediaPlayer.setDataSource(file.getAbsolutePath());
            // 设置显示视频的SurfaceHolder
            mediaPlayer.setDisplay(sv.getHolder());
            Log.i(TAG, "开始装载");
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG, "装载完成");
                    mediaPlayer.start();
                    // 按照初始位置播放
                    mediaPlayer.seekTo(msec);
                    // 设置进度条的最大进度为视频流的最大播放时长
                    seekBar.setMax(mediaPlayer.getDuration());
                    // 开始线程，更新进度条的刻度
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                isPlaying = true;
                                while (isPlaying) {
                                    int current = mediaPlayer
                                            .getCurrentPosition();
                                    seekBar.setProgress(current);

                                    sleep(500);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // 在播放完毕被回调
                    iv_play.setVisibility(View.VISIBLE);
                    iv_pause.setVisibility(View.GONE);
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // 发生错误重新播放
                    play(0);
                    isPlaying = false;
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            iv_pause.setVisibility(View.GONE);
            iv_play.setVisibility(View.VISIBLE);
            Toast.makeText(this, "暂停播放", Toast.LENGTH_SHORT).show();
        }
    }
    
    protected void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
//            btn_play.setEnabled(true);
            iv_play.setVisibility(View.VISIBLE);
            isPlaying = false;
        }
    }
    
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sv:
                if (iv_play.getVisibility() != View.VISIBLE) {
                    iv_pause.setVisibility(View.VISIBLE);
                }
                
                iv_pause.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (iv_pause != null) {
                            iv_pause.setVisibility(View.GONE);
                        }
                    }
                },3000);
                break;
            case R.id.iv_play:
                mediaPlayer.start();
                Toast.makeText(this, "开始播放", Toast.LENGTH_SHORT).show();
                iv_play.setVisibility(View.GONE);
                break;
            case R.id.iv_pause:
                pause();
                break;
        }
    }
}
