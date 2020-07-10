package com.nmpa.nmpaapp.modules.analysis;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class ChartUtils {
  /*
    *  柱状图初始化
    *  size 为数据的条数
    *  isLegend 为是否显示图例
    *  isSingle 为是否为单柱状图
    * */
    public static void initBarChart(BarChart mBarChart, int size, boolean isLegend, boolean isSingle) {
        mBarChart.setExtraOffsets(0, 20, 20, 20);
        //初始化
        mBarChart.setBackgroundColor(Color.WHITE);
        //不显示图表网格
        mBarChart.setDrawGridBackground(false);
        //背景阴影
        mBarChart.setDrawBarShadow(false);
        mBarChart.setHighlightFullBarEnabled(false);
        //显示边框
        mBarChart.setDrawBorders(true);
        // 二指控制X轴Y轴同时放大
        mBarChart.setPinchZoom(false);
        mBarChart.setDoubleTapToZoomEnabled(false);
        //设置X轴显示文字旋转角度-60意为逆时针旋转60度
        mBarChart.getXAxis().setLabelRotationAngle(-30);
        // 设置最大可见Value值的数量 针对于ValueFormartter有效果
        mBarChart.setMaxVisibleValueCount(size+10);
        mBarChart.setFitBars(false);
        //        mBarChart.setKeepPositionOnRotation(false);
        mBarChart.fitScreen();

        /* X轴的设置 */
        XAxis xAxis = mBarChart.getXAxis();
        //设置X轴显示位置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //X轴横坐标显示的数量
        xAxis.setLabelCount(size);
        //不显示X轴网格线
        xAxis.setDrawGridLines(false);
        //不显示X轴线条
        xAxis.setDrawAxisLine(false);
//        xAxis.setYOffset(150);
//        xAxis.setXOffset(10);
//        xAxis.setGranularity(1f);//设置最小间隔，防止当放大时，出现重复标签。
//        mBarChart.fitScreen();
        Log.e("ChartUtils",""+xAxis.getAxisMinimum());
        if (!isSingle) {
            xAxis.setAxisMaximum(size);
            xAxis.setAxisMinimum(0f);
            xAxis.setCenterAxisLabels(true);
        }
      
        /* Y轴设置 */
        YAxis leftAxis = mBarChart.getAxisLeft();
        YAxis rightAxis = mBarChart.getAxisRight();
        //不显示X轴 Y轴线条
        leftAxis.setDrawAxisLine(true);
        rightAxis.setDrawAxisLine(false);
//        leftAxis.setXOffset(0);
        leftAxis.setYOffset(0);
        //不显示左侧Y轴
        rightAxis.setEnabled(false);
        //右侧Y轴网格线设置为虚线
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        //设置Y左边轴显示的值 label 数量
        leftAxis.setLabelCount(7, true);
        leftAxis.setAxisMinValue(0);
        //设置值显示的位置，我们这里设置为显示在Y轴外面
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        //设置Y轴 与值的空间空隙 这里设置30f意为30%空隙，默认是10%
        leftAxis.setSpaceTop(0f);
//        leftAxis.setAxisMinimum(0f);//设置Y轴最小值
        /***折线图例 标签 设置***/
        Legend legend = mBarChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(11f);
        //显示位置
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setYOffset(0);
        //是否绘制在图表里面
        legend.setDrawInside(false);
        legend.setEnabled(isLegend);

        //不显示表边框
        mBarChart.setDrawBorders(false);
//        不显示右下角描述内容
        Description description = new Description();
        description.setEnabled(false);
        mBarChart.setDescription(description);
      //        setBarChartData(mBarChart);
    }
  //    private void setBarChartData(BarChart mBarChart) {
//        List<BarEntry> barEntries = new ArrayList<>();
//        List<BarEntry> barEntries1 = new ArrayList<>();
//        for (int i= 0; i< collectDataList.size(); i++) {
//            CollectData bean = collectDataList.get(i);
//            BarEntry barEntry = new BarEntry(i,bean.getValue());
//            barEntry.setData(bean);
//            barEntries.add(barEntry);
//        }
//        for (int i= 0; i< collectDataList.size(); i++) {
//            CollectData bean = collectDataList.get(i);
//            BarEntry barEntry = new BarEntry(i,bean.getValueInspect());
//            barEntry.setData(bean);
//            barEntries1.add(barEntry);
//        }
//        List<IBarDataSet> barDataSets = new ArrayList<>();
//        BarDataSet barDataSet = new BarDataSet(barEntries,"111");
//        barDataSet.setColor(getResources().getColor(R.color.color_shop_title));
//        barDataSet.setFormSize(15.f);
////        barDataSet.setFormLineWidth(1f);
//        barDataSet.setDrawValues(false);
//        BarDataSet barDataSet1 = new BarDataSet(barEntries1,"222");
//        barDataSet1.setColor(getResources().getColor(R.color.color_expire));
//        barDataSet1.setFormSize(15.f);
////        barDataSet1.setFormLineWidth(1f);
//        barDataSet1.setDrawValues(false);
//        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
//        barDataSet1.setAxisDependency(YAxis.AxisDependency.LEFT);
//        barDataSets.add(barDataSet);
//        barDataSets.add(barDataSet1);
//        BarData data = new BarData(barDataSet,barDataSet1);
//        mBarChart.setData(data);
//        // 设置 柱子宽度
////        float barWidth = 0.4f;
////        float groupSpace = 0.2f;
////        float barSpace = 0.00f;
//        float groupSpace = 0.3f; //柱状图组之间的间距
//        float barSpace = (float) ((1 - 0.12) / 2 / 10); // x4 DataSet
//        float barWidth = (float) ((1 - 0.3) / 2 / 10 * 9); // x4 DataSet
//
//        data.setBarWidth(barWidth);
////        Log.e(TAG,"value = "+((barWidth + barSpace) * 2 + groupSpace));
//        mBarChart.groupBars(0f, groupSpace, barSpace);
//        mBarChart.invalidate();
//    }
    /*
    * 饼图初始化
    * */
    public static void initPieChart(PieChart pc_collect, boolean isHole) {

        pc_collect.setUsePercentValues(true);
        pc_collect.getDescription().setEnabled(false);
        //设置整个饼图的偏移
        pc_collect.setExtraOffsets(0,10,5,5);

        pc_collect.setDragDecelerationEnabled(true);
        pc_collect.setDragDecelerationFrictionCoef(0.95f);
        //设置中间文字
//        pc_collect.setCenterText(generateCenterSpannableText());

        //中间圆心显示与否以及颜色，默认true
        pc_collect.setDrawHoleEnabled(isHole);
        pc_collect.setHoleColor(Color.WHITE);

        //半透明圆环的颜色和透明度
        pc_collect.setTransparentCircleColor(Color.WHITE);
        pc_collect.setTransparentCircleAlpha(110);
        //设置中间圆心，如果setTransparentCircleRadius的值比setHoleRadius小，只显示一套圆环
//        pc_collect.setHoleRadius(58f);
        //内圆环的半径
        pc_collect.setTransparentCircleRadius(41f);

        //是否显示中间文字，默认是true
        pc_collect.setDrawCenterText(false);

        //旋转角度
        pc_collect.setRotationAngle(0);

        //触摸旋转
        pc_collect.setRotationEnabled(false);
        //点击放大
        pc_collect.setHighlightPerTapEnabled(true);
        //数据
        pc_collect.setNoDataText("暂无数据");

//        List<PieEntry> entries = new ArrayList<>();
//        for (int i=0;i<collectDataBLList.size(); i++) {
//            CollectDataBL collectDataBL = collectDataBLList.get(i);
//            PieEntry pieEntry = new PieEntry(collectDataBL.getOfficeCount(),collectDataBL.getType());
//            entries.add(pieEntry);
//        }
//        setData(entries,pc_collect);
        pc_collect.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        //图表的标注，默认在最下方横排显示，此设置在右上角竖排显示
        Legend l = pc_collect.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        //输入标签样式
        pc_collect.setEntryLabelColor(Color.WHITE);
        pc_collect.setEntryLabelTextSize(12f);
    }

    public static void setPieData(List<PieEntry> entries, PieChart pieChart) {
        PieDataSet dataSet = new PieDataSet(entries,"");
        //设置圆环之间的距离
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        //数据和颜色
         List<Integer> colors = new ArrayList<>();
        //5个for循环都可以用，每组颜色都不同
//        for (int c : ColorTemplate.VORDIPLOM_COLORS) {
//            colors.add(c);
//        }

//        for (int c : ColorTemplate.JOYFUL_COLORS) {
//            colors.add(c);
//        }
//
        for (int c : ColorTemplate.COLORFUL_COLORS) {
            colors.add(c);
        }
//
//        for (int c : ColorTemplate.LIBERTY_COLORS) {
//            colors.add(c);
//        }
//
        for (int c : ColorTemplate.PASTEL_COLORS) {
            colors.add(c);
        }
//
//        //每个圆环的颜色一样，都是蓝色
//        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        pieChart.setData(data);
        pieChart.highlightValue(null);
        pieChart.invalidate();
    }
}
