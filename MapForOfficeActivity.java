package com.nmpa.nmpaapp.modules.office;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.base.BaseActivity;
public class MapForOfficeActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MapForOfficeActivity";

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    public LocationClient mLocClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    boolean isFirstLoc = true; // 是否首次定位
    private double mLatitude;
    private double mLongitude;
    private String mAddress;

    private void initMap() {

        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, BitmapDescriptorFactory.fromResource(R.mipmap.icon_gcoding),
                0x0f1679b3, 0xAA00FF00));
       // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true); // 打开gps
        option.setIsNeedAddress(true);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(3000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    @Override
    public void onBeforeSetContentView() {

    }
    @Override
    public int getLayoutResID() {
        return R.layout.activity_map_for_office;
    }

    @Override
    public void initContentView(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mLatitude = getIntent().getDoubleExtra("latitude", 0.0f);
        mLongitude = getIntent().getDoubleExtra("longitude", 0.0f);
        mAddress = getIntent().getStringExtra("address");
        Log.e(TAG,"mAddress="+mAddress);
        if (mAddress != null && !mAddress.equals("")) {
            initSearch();
        }
        initView();
        initMap();
    }
    
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            String province = location.getProvince();    //获取省份
            String city = location.getCity();    //获取城市
            String district = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息
            mAddress = province + city + district + street;
            //获取纬度信息
//            mLatitude = location.getLatitude();
//            //获取经度信息
//            mLongitude = location.getLongitude();

            Log.d("flag", "onReceiveLocation: " + mLatitude + ":" + mLongitude);
            if(mLatitude == 0.0 && mLongitude == 0.0) {
                MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(location.getDirection())
//                    .direction(100)
                    .latitude(location.getLatitude()).longitude(location.getLongitude())
                        .build();
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();
                mBaiduMap.setMyLocationData(locData);
            } else {
                MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(location.getDirection())
                        .latitude(mLatitude).longitude(mLongitude)
                        .build();
                mBaiduMap.setMyLocationData(locData);
            }

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(mLatitude, mLongitude);
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            // 当不需要定位图层时关闭定位图层
//            mBaiduMap.setMyLocationEnabled(false);

        }

    }
    
    private void initView() {
        TextView title = findViewById(R.id.title);
        title.setText("地图");
        LinearLayout back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        
        TextView type_normal = findViewById(R.id.type_normal);
        TextView type_satellite = findViewById(R.id.type_satellite);
        TextView map_position = findViewById(R.id.map_position);
        TextView confirm = findViewById(R.id.confirm);
        type_normal.setOnClickListener(this);
        type_satellite.setOnClickListener(this);
        map_position.setOnClickListener(this);
        confirm.setOnClickListener(this);
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.e(TAG, "onMapClick latitude: "+latLng.latitude );//经度
            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
              Log.e(TAG, "onMapPoiClick latitude: "+mapPoi.getName()+","+mapPoi.getPosition()+","+mapPoi.getUid() );//经度
                LatLng latLng = mapPoi.getPosition();
                setMarkPoint(latLng.latitude,latLng.longitude);
                mLatitude = latLng.latitude;
                mLongitude = latLng.longitude;
            }
        });

    }
    
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.type_normal:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.type_satellite:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.map_position:
                mLocClient.start();
                break;
            case R.id.confirm:
                Intent intent = new Intent();
                intent.putExtra("latitude", mLatitude);
                intent.putExtra("longitude", mLongitude);
                intent.putExtra("address", mAddress);
                Log.d("flag", "onClick: "+mLatitude+":"+mLongitude+":"+mAddress);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    LatLng point;
    private void setMarkPoint(double jingdu,double weidu) {
        //定义Maker坐标点
        mBaiduMap.clear();
        point = new LatLng(jingdu, weidu);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.mipmap.icon_gcoding);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
    }
    
    GeoCoder mSearch;
    private void initSearch() {
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                Log.e(TAG, "onGetGeoCodeResult latitude: "+geoCodeResult.getLocation() );//经度
                LatLng latLng = geoCodeResult.getLocation();
                
                if (latLng != null) {
                    mLatitude = latLng.latitude;
                    mLongitude = latLng.longitude;
                }
                isFirstLoc = true;
//                setMarkPoint(mLatitude, mLongitude);
//                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(geoCodeResult
//                        .getLocation()));
//                mLocClient.start();
            }
            
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                Log.e(TAG, "onGetReverseGeoCodeResult latitude: "+reverseGeoCodeResult.getLocation() );//经度
            }
        });
        mSearch.geocode(new GeoCodeOption().city(mAddress).address(mAddress));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mLocClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
    }
}
