package com.nmpa.nmpaapp.modules.monitor;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.base.BaseActivity;
import com.nmpa.nmpaapp.base.ui.AppBarConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;

public class BackPlayerListActivity extends BaseActivity {
    private static final String TAG = "BackPlayerListActivity";
    private Context mContext;
    @BindView(R.id.prr_record)
    RecyclerView prr_record;
    private List<PlayBean> playBeans = new ArrayList<>();
    @Override
    public void onBeforeSetContentView() {

    }

    @Override
    public int getLayoutResID() {
        return R.layout.activity_back_player_list;
    }

    @Override
    protected CharSequence setActionBarTitle() {
        return "视频录制列表";
    }
    
    @Nullable
    @Override
    public AppBarConfig getAppBarConfig() {
        return mAppBarCompat;
    }

    @Override
    public void initContentView(@Nullable Bundle savedInstanceState) {
        mContext = BackPlayerListActivity.this;

    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        getLocalRecordData();
    }
    
    String video_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/";
    File[] files = {};
    private void getLocalRecordData() {

        File file = new File(video_path);
        //判断文件夹是否存在，如果不存在就创建一个
        if (!file.exists()) {
            file.mkdirs();
        }
        files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".mp4")) {
                File file1 = files[i];
                PlayBean playBean = new PlayBean();
                playBean.setName(file1.getName());
                playBean.setPath(file1.getPath());
                String date = file1.getName().substring(4,18);
                String recordDate = date.substring(0,4)+"-"+date.substring(4,6)+"-"+date.substring(6,8)+" "+date.substring(8,10)+":"+date.substring(10,12);
                Log.e(TAG,"date="+date+","+recordDate);
                playBean.setRecordDate(recordDate);
                playBeans.add(playBean);
            }
        }
        BackPlayAdapter backPlayAdapter = new BackPlayAdapter(mContext, playBeans);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(RecyclerView.VERTICAL);
        prr_record.setLayoutManager(manager);
        prr_record.setAdapter(backPlayAdapter);
    }
}
