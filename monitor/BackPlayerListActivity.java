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
}
