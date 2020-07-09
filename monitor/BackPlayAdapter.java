package com.nmpa.nmpaapp.modules.monitor;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.android.arouter.launcher.ARouter;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.router.Page;
import java.util.List;

public class BackPlayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "CheckPlanAdapter";
    private Context mContext;
    private List<PlayBean> playBeans;
}
