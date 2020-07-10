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
    private List<TypeLBean> areaLBeans = new ArrayList<>();
    @BindView(R.id.pc_type)
    PieChart pc_type;
    @BindView(R.id.pc_office)
    PieChart pc_office;
    @BindView(R.id.bc_area)
    BarChart bc_area;

    @Override
    public void onBeforeSetContentView() {

    }
    @Override
    public int getLayoutResID() {
        return R.layout.activity_mingchu;
    }

    @Override
    protected CharSequence setActionBarTitle() {
        return "明厨亮灶建设";
    }

    @Nullable
    @Override
    public AppBarConfig getAppBarConfig() {
        return mAppBarCompat;
    }
    
    @Override
    public void initContentView(@Nullable Bundle savedInstanceState) {
        mContext = MingchuActivity.this;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        getMingchuData();
    }

    private void getMingchuData() {
        HashMap<String, Object> baseParam = WebFrontUtil.getBaseParam();
        OkHttpUtil.post(TAG, WebApi.ACTIVITY_ANALYSIS_MINGCHU, baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.d(TAG,"getMingchuData e="+e);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d(TAG,"getMingchuData response="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    if ((int)object.get("code") == 200) {
                        JSONObject object1 = object.getJSONObject("data");
                        if (object1 == null) {
                            pc_office.setVisibility(View.GONE);
                            pc_type.setVisibility(View.GONE);
                            bc_area.setVisibility(View.GONE);
                            return;
                        }
                        Gson gson = new Gson();
                        JSONObject echartD = object1.getJSONObject("echartD");
                        TypeLBean echartBean = gson.fromJson(echartD.toString(), TypeLBean.class);
                        PieEntry pieEntry1 = new PieEntry((echartBean.getCountNum()-echartBean.getCountCamNum()),"非明亮单位");
                        PieEntry pieEntry2 = new PieEntry(echartBean.getCountCamNum(),"明亮单位");
                        List<PieEntry> eChartEntries = new ArrayList<>();
                        eChartEntries.add(pieEntry1);
                        eChartEntries.add(pieEntry2);
                        ChartUtils.initPieChart(pc_office,true);
                        ChartUtils.setPieData(eChartEntries, pc_office);

                        JSONArray array = object1.getJSONArray("typeL");

                        if (array.length() > 0) {
                            List<PieEntry> typeEntries = new ArrayList<>();
                            for (int i= 0;i<array.length();i++) {
                                TypeLBean resultBean = gson.fromJson(array.get(i).toString(), TypeLBean.class);
                                typeLBeans.add(resultBean);
                                PieEntry pieEntry = new PieEntry(resultBean.getTypeCount(),resultBean.getCateringTypeLabel());
                                typeEntries.add(pieEntry);
                            }
                            ChartUtils.initPieChart(pc_type,false);
                            ChartUtils.setPieData(typeEntries, pc_type);
                        } else {
                            pc_type.setVisibility(View.GONE);
                        }

                        JSONArray array1 = object1.getJSONArray("areaL");
                        if (array1.length() > 0) {
                            List<PieEntry> areaEntries = new ArrayList<>();
                            for (int i= 0;i<array1.length();i++) {
                                TypeLBean resultBean = gson.fromJson(array1.get(i).toString(), TypeLBean.class);
                                areaLBeans.add(resultBean);
//                            BarEntry barEntry = new BarEntry(i,resultBean.getCountNum());
                            }
                            ChartUtils.initBarChart(bc_area,areaLBeans.size(),true,false);
                            setBarChartData(bc_area);
                        } else {
                            bc_area.setVisibility(View.GONE);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void setBarChartData(BarChart mBarChart) {
        List<BarEntry> barEntries = new ArrayList<>();
        List<BarEntry> barEntries1 = new ArrayList<>();
        for (int i= 0; i< areaLBeans.size(); i++) {
            TypeLBean bean = areaLBeans.get(i);
            BarEntry barEntry = new BarEntry(i,bean.getCountNum());
            barEntry.setData(bean);
            barEntries.add(barEntry);
        }
        for (int i= 0; i< areaLBeans.size(); i++) {
            TypeLBean bean = areaLBeans.get(i);
            BarEntry barEntry = new BarEntry(i,bean.getCountCamNum());
            barEntry.setData(bean);
            barEntries1.add(barEntry);
        }

        //X轴自定义值
        XAxis xAxis = bc_area.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value < 0) {
                    return "";
                }
                String labelValue = areaLBeans.get((int) value % areaLBeans.size()).getAreaName();
                if (labelValue.length() > 4) {
                    labelValue = labelValue.substring(0,4)+"…";
                }
                return labelValue;
            }
        });
        List<IBarDataSet> barDataSets = new ArrayList<>();
        BarDataSet barDataSet = new BarDataSet(barEntries,"单位总数");
        barDataSet.setColor(getResources().getColor(R.color.color_shop_title));
        barDataSet.setFormSize(10.f);
//        barDataSet.setFormLineWidth(1f);
        barDataSet.setDrawValues(false);
        BarDataSet barDataSet1 = new BarDataSet(barEntries1,"明亮单位");
        barDataSet1.setColor(getResources().getColor(R.color.color_expire));
        barDataSet1.setFormSize(10.f);
//        barDataSet1.setFormLineWidth(1f);
        barDataSet1.setDrawValues(false);
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        barDataSet1.setAxisDependency(YAxis.AxisDependency.LEFT);
        barDataSets.add(barDataSet);
        barDataSets.add(barDataSet1);
        BarData data = new BarData(barDataSet,barDataSet1);
        mBarChart.setData(data);
        float groupSpace = 0.3f; //柱状图组之间的间距
        float barSpace = (float) ((1 - 0.12) / 2 / 10); // x4 DataSet
        float barWidth = (float) ((1 - 0.3) / 2 / 10 * 9); // x4 DataSet

        data.setBarWidth(barWidth);
        Log.e(TAG,"value = "+((barWidth + barSpace) * 2 + groupSpace));
        mBarChart.groupBars(0f, groupSpace, barSpace);
//        mBarChart.invalidate();
    }
}
