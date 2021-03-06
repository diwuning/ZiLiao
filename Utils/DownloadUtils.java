package com.nmpa.nmpaapp.modules.update;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.base.BaseApplication;
import com.nmpa.nmpaapp.widget.DownloadProgressButton;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnBindViewListener;
import com.timmy.tdialog.listener.OnViewClickListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
public class DownloadUtils {
    private static final String TAG = "DownloadUtils";

    private Context mContext;
    private FragmentManager mFragmentManager;
    private File mFile;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mDownloadProgress != null) {
                      float progress = (float) msg.obj;
                        if (progress < mDownloadProgress.getMaxProgress()) {
                            mDownloadProgress.setState(DownloadProgressButton.STATE_DOWNLOADING);
                            mDownloadProgress.setProgressText("下载中", progress);
                        } else {
                            mDownloadProgress.setState(DownloadProgressButton.STATE_FINISH);
                            mDownloadProgress.setCurrentText("下载完成，点击安装");
                            mDownloadProgress.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mFile != null) {
                                        installApk(mFile);
                                    }
                                }
                            });
                        }
                    }
                    break;
            }
        }
    };
    
    private DownloadProgressButton mDownloadProgress;
    private long[] mHits = new long[4];
    private boolean finish = true;
    public DownloadUtils(Context context, FragmentManager fragmentManager, String url, String content) {
        mContext = context;
        mFragmentManager = fragmentManager;
        if (url != null && !url.equals("")) {
            initDialog(content);
            downloadFile(url);
        }
    }
    
    private void initDialog(String content) {
        content = content == null || content.equals("")
                ? "发现新版本，为了不影响您的使用，正在升级..." : content;

        final String finalContent = content;
        new TDialog.Builder(mFragmentManager)
                .setLayoutRes(R.layout.dialog_update_progress)
                .setScreenWidthAspect(((Activity) mContext), 0.8f)
                .setTag("DialogTest")
                .setDimAmount(0.6f)
                .setGravity(Gravity.CENTER)
                .setCancelableOutside(false)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (finish) {
                            ActivityCollector.finishAll();
                        }
                    }
                })
                .setOnBindViewListener(new OnBindViewListener() {
                    @Override
                    public void bindView(BindViewHolder bindViewHolder) {
                        bindViewHolder.setText(R.id.tv_content, finalContent);
                        mDownloadProgress = bindViewHolder.getView(R.id.download_bar);
                        mDownloadProgress.setButtonRadius(30);
                        mDownloadProgress.setShowBorder(false);
                        mDownloadProgress.postInvalidate();
                        mDownloadProgress.setState(DownloadProgressButton.STATE_DOWNLOADING);
                        mDownloadProgress.setProgressText("下载中", 0);
                    }
                })
                .addOnClickListener(R.id.tv_title)//R.id.tv_cancel, R.id.tv_confirm,
                .setOnViewClickListener(new OnViewClickListener() {
                    @Override
                    public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                        switch (view.getId()) {
                          case R.id.tv_title:
                                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                                if (mHits[0] >= (SystemClock.uptimeMillis() - 1000)) {
                                    tDialog.dismiss();
                                    finish = false;
                                }
                                break;
                                }
                    }
                })
                .create()
                .show();
    }

    private void downloadFile(final String url) {
      new Thread() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setConnectTimeout(5000);
                    //获取到文件的大小
//                    mDownloadProgress.setMaxProgress(conn.getContentLength());
                    InputStream is = conn.getInputStream();
                    mFile = new File(getRoot(), "nmpa.apk");
//                    mFile = new File(getRoot(), "firef.apk");
                    FileOutputStream fos = new FileOutputStream(mFile);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    byte[] buffer = new byte[1024];
                    int len;
                    int total = 0;
                    float c = 0;
                    while ((len = bis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        //获取当前下载量
                        total += len;
                        float nextC = ((float) total / conn.getContentLength()) * 100;
                        Message message = mHandler.obtainMessage();
                        if (message == null) {
                            message = new Message();
                        }
                        if (nextC - c > 0.1) {
                            message.what = 0;
                            message.obj = nextC;
                            mHandler.sendMessage(message);
                            c = nextC;
                        } else if (total == conn.getContentLength()) {
                            message.what = 0;
                            message.obj = nextC;
                            mHandler.sendMessage(message);
                            c = nextC;
                        }
                    }
                    fos.close();
                    bis.close();
                    is.close();
                    installApk(mFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    
    //安装apk
    private void installApk(File file) {
        Log.e(TAG,"file="+file);
        //版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(mContext, "com.nmpa.nmpaapp.file.provider", file);//在AndroidManifest中的android:authorities值
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            mContext.startActivity(install);
        } else {
           Intent intent = new Intent();
            //执行动作
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //执行的数据类型
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            mContext.startActivity(intent);
        }
    }
    
    private File getRoot() {
        String dir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        } else {
            dir = new BaseApplication().getCacheDir().getAbsolutePath();
        }
        
        File root = new File(dir, "nmpa");
        if (!root.exists()) {
            root.mkdirs();
        }
        return root;
    }
                            
    // 开启安装未知来源权限
    public static void toInstallPermissionSettingIntent() {
        Uri packageURI = Uri.parse("package:"+getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        startActivityForResult(intent, 100);
    }
}
