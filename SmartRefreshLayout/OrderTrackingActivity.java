import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.Marker;
import com.jnluke.xiaoeryabiao.R;
import com.jnluke.xiaoeryabiao.base.BaseActivity;
import com.jnluke.xiaoeryabiao.dialog.SelectAddressSecondDialog;
import com.jnluke.xiaoeryabiao.dialog.SelectDateTimeDialog;
import com.jnluke.xiaoeryabiao.http.ApiFactory;
import com.jnluke.xiaoeryabiao.http.SimpleObserver;
import com.jnluke.xiaoeryabiao.me.bean.ShengListBean;
import com.jnluke.xiaoeryabiao.me.bean.ShiListBean;
import com.jnluke.xiaoeryabiao.order.adapter.OrderTrackingAdapter;
import com.jnluke.xiaoeryabiao.order.bean.OrderDataBean;
import com.jnluke.xiaoeryabiao.order.bean.OrderDirectionBean;
import com.jnluke.xiaoeryabiao.order.bean.OrderListBean;
import com.jnluke.xiaoeryabiao.order.bean.OrderRequestBean;
import com.jnluke.xiaoeryabiao.order.bean.OrderReturnBean;
import com.jnluke.xiaoeryabiao.order.bean.PageBean;
import com.jnluke.xiaoeryabiao.utils.ReqUtils;
import com.jnluke.xiaoeryabiao.widget.ItemDecoration;
import com.jnluke.xiaoeryabiao.widget.TimeSelector;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/***
 * 订单追踪列表
 */
public class OrderTrackingActivity extends BaseActivity implements OnRefreshLoadMoreListener, View.OnClickListener {
    @BindView(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.mRefreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.ll_start_point)
    RelativeLayout startPoint;
    @BindView(R.id.startPointTv)
    TextView startPointTv;
    @BindView(R.id.ll_destination)
    RelativeLayout destination;
    @BindView(R.id.destinationTv)
    TextView destinationTv;
    @BindView(R.id.sendTime)
    TextView sendTime;
    @BindView(R.id.ll_delivery_time)
    RelativeLayout deliveryTime;
    @BindView(R.id.map_view)
    MapView mMapView;
    //初始化地图控制器对象
    AMap aMap;
    String TAG = getClass().getSimpleName();
    private int pageSize = 10;
    private int pageNum = 1;
    private OrderTrackingAdapter orderTrackingAdapter;
    private Marker marker;
    private List<OrderDirectionBean> orderDirectionBeans = new ArrayList<>();
    private boolean isFirst = true;
    private String chooseFlag = "";
    private String startCityCode = "";
    private String endCityCode = "";
    private String endDate = "";

    @Override
    public int getLayoutResId() {
        return R.layout.activity_order_tracking;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this);
        mTvTitle.setText("订单追踪");
        mMapView.onCreate(savedInstanceState);
        startPoint.setOnClickListener(this);
        destination.setOnClickListener(this);
        deliveryTime.setOnClickListener(this);

        mRefreshLayout.setOnRefreshLoadMoreListener(this);
        mRefreshLayout.setEnableLoadMore(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(OrderTrackingActivity.this));
        mRecyclerView.addItemDecoration(new ItemDecoration(10));
        orderTrackingAdapter = new OrderTrackingAdapter(OrderTrackingActivity.this, R.layout.item_order_list, list);
        mRecyclerView.setAdapter(orderTrackingAdapter);
        getData("1", "1");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    List<OrderListBean> list = new ArrayList<>();

    private TextView carNumTv;
    private TextView orderStatusTv;
    private LinearLayout markLay;


    private void getData(String type, String orderStatus) {
//        showLoading();
        OrderDirectionBean order = new OrderDirectionBean();
        order.setField("id");
        order.setDirection("DESC");
        orderDirectionBeans.add(order);
        OrderRequestBean orderRequestBean = new OrderRequestBean();
        orderRequestBean.setType(type);
        orderRequestBean.setEndCity(endCityCode);
        orderRequestBean.setStartCity(startCityCode);
//        orderRequestBean.setEndDate(endDate);
        orderRequestBean.setOrderPlaceTime(endDate);
        orderRequestBean.setOrderStatus(orderStatus);
        PageBean pageBean = new PageBean();
        pageBean.setPageNumber(pageNum);
        pageBean.setPageSize(pageSize);
        orderRequestBean.setPage(pageBean);
        pageBean.setOrders(orderDirectionBeans);
        String requestString = ReqUtils.getJson(orderRequestBean);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestString);
        ApiFactory.getApi().getOrderTrackList(requestBody).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new SimpleObserver<OrderReturnBean>() {


            @Override
            public void onSuccess(OrderReturnBean resp) {
//                hideLoading();
                Log.e("resp", resp.toString());
                mRefreshLayout.finishLoadMore();
                mRefreshLayout.finishRefresh();
                OrderDataBean orderDataBean = resp.getData();
                if (orderDataBean.getList().size() < pageSize) {
                    mRefreshLayout.setEnableLoadMore(false);
                } else {
                    mRefreshLayout.setEnableLoadMore(true);
                }
                if (pageNum == 1) {
                    list.clear();
                }

                list.addAll(orderDataBean.getList());
//                addMarkersToMap(list);
                orderTrackingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(String errCode, String msg) {
//                hideLoading();
                mRefreshLayout.finishLoadMore();
                mRefreshLayout.finishRefresh();
                showToast(msg);
            }
        });
    }


    @Override
    public void onLoadMore(RefreshLayout refreshLayout) {
        pageNum++;
        getData("1", "1");
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        pageNum = 1;
        getData("1", "1");
    }

    private TimeSelector timeSelector;

    @Override
    public void onClick(View v) {
        orderDirectionBeans.clear();
        switch (v.getId()) {
            case R.id.ll_start_point:

                SelectAddressSecondDialog.getInstance().setContext(this)
                        .setFlag(startPointTv).setCallback(new SelectAddressSecondDialog.Callback() {
                    @Override
                    public void onCallback(View flag, ShengListBean.DataBean provinceBean, ShiListBean.DataBean cityBean) {
                        if (null == provinceBean && null == cityBean) {
                            startCityCode = "";
                            startPointTv.setText("发货地");
                        } else {
                            startCityCode = cityBean.getCityCode();
                            startPointTv.setText(cityBean.getCity());
                        }
                        pageNum = 1;
                        getData("1", "1");
                    }
                }).setShowBottom(true)
                        .show(getSupportFragmentManager());
                break;
            case R.id.ll_destination:
                SelectAddressSecondDialog.getInstance().setContext(this)
                        .setFlag(startPointTv).setCallback(new SelectAddressSecondDialog.Callback() {
                    @Override
                    public void onCallback(View flag, ShengListBean.DataBean provinceBean, ShiListBean.DataBean cityBean) {
                        if (null == provinceBean && null == cityBean) {
                            endCityCode = "";
                            destinationTv.setText("目的地");
                        } else {
                            endCityCode = cityBean.getCityCode();
                            destinationTv.setText(cityBean.getCity());
                        }
                        pageNum = 1;
                        getData("1", "1");
                    }
                }).setShowBottom(true)
                        .show(getSupportFragmentManager());
                break;
            case R.id.ll_delivery_time:
                SelectDateTimeDialog.getInstance().setContext(this).setShowTimer(false).setOnCallback(new SelectDateTimeDialog.OnCallback() {
                    @Override
                    public void onCallback(String dateTime) {
                        if (!TextUtils.isEmpty(dateTime)) {
                            endDate = dateTime.trim();
                            sendTime.setText(endDate);
                        }
                        pageNum = 1;
                        getData("1", "1");
                    }
                }).show();

                break;
            default:
                break;
        }
    }
}
