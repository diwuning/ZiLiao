package com.nmpa.nmpaapp.modules.analysis.mingchu;

import androidx.annotation.Nullable;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.gson.Gson;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.base.BaseActivity;
import com.nmpa.nmpaapp.base.ui.AppBarConfig;
import com.nmpa.nmpaapp.constants.WebApi;
import com.nmpa.nmpaapp.http.OkHttpUtil;
import com.nmpa.nmpaapp.http.WebFrontUtil;
import com.nmpa.nmpaapp.modules.analysis.ChartUtils;
import com.nmpa.nmpaapp.router.Page;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import okhttp3.Call;

@Route(path = Page.ACTIVITY_ANALYSIS_MINGCHU)
public class MingchuActivity extends BaseActivity {
    private static final String TAG = "MingchuActivity";
    private Context mContext;
    private List<TypeLBean> typeLBeans = new ArrayList<>();
}
