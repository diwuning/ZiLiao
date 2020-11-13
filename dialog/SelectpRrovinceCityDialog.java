import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidybp.basics.fastjson.JsonManager;
import com.androidybp.basics.okhttp3.OkgoUtils;
import com.androidybp.basics.utils.hint.ToastUtil;
import com.escort.carriage.android.R;
import com.escort.carriage.android.configuration.ProjectUrl;
import com.escort.carriage.android.entity.request.RequestEntity;
import com.escort.carriage.android.entity.response.home.QuListBean;
import com.escort.carriage.android.entity.response.home.ShengListBean;
import com.escort.carriage.android.entity.response.home.ShiListBean;
import com.escort.carriage.android.http.MyStringCallback;
import com.escort.carriage.android.ui.widget.TagCloudView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * 级联选择地址  可多选
 */
public class SelectpRrovinceCityDlg extends BaseDialogFragment {
    public static final int CITY_TAGS_MAX_INT = 6;
    private Context mContext;
    private LinearLayout llBoxProvince;
    private LinearLayout llBoxCity;
    private LinearLayout llBoxArea;

    private List<ShengListBean.DataBean> mProvinceList = new ArrayList<>();
    private List<ShiListBean.DataBean> mCityList = new ArrayList<>();
    private List<QuListBean.DataBean> mAreaList = new ArrayList<>();

    private List<TextView> mProvinceTvList = new ArrayList<>();
    private List<TextView> mCityTvList = new ArrayList<>();
    private List<TextView> mAreaTvList = new ArrayList<>();
    private ShengListBean.DataBean mCurrentProvinceBean;
    private ShiListBean.DataBean mCurrentCityBean;
    private QuListBean.DataBean mCurrentAreaBean;
    private View mFlag;
    private ScrollView areaView;
    private TagCloudView mTagCloudView;
    private List<String> mCityTags = new ArrayList<>();
    private ArrayList<QuListBean.DataBean> mSelectedBeans = new ArrayList<>();

    private boolean isVisiable = false;


    public SelectpRrovinceCityDlg setVisiableOfView(boolean hasArea) {
        isVisiable = hasArea;
        return this;
    }

    public static SelectpRrovinceCityDlg getInstance() {
        return new SelectpRrovinceCityDlg();
    }

    public SelectpRrovinceCityDlg setContext(Context context) {
        mContext = context;
        return this;
    }

    public SelectpRrovinceCityDlg setContext(Context context,ArrayList<QuListBean.DataBean> mSelectedBeans) {
        mContext = context;
        this.mSelectedBeans = mSelectedBeans;
        return this;
    }

    public SelectpRrovinceCityDlg setFlag(View object) {
        mFlag = object;
        return this;
    }

    public SelectpRrovinceCityDlg setData(ArrayList<QuListBean.DataBean> mSelectedBeans) {
        this.mSelectedBeans = mSelectedBeans;
        for (int i=0;i<mSelectedBeans.size();i++) {
            QuListBean.DataBean dataBean = mSelectedBeans.get(i);
            if (dataBean.getProvinceCode().equals(dataBean.getCityCode())) {
                mCityTags.add(dataBean.getProvinceName());
            } else {
                if (dataBean.getCityCode() != dataBean.getAreaCode()) {
                    mCityTags.add(dataBean.getArea());
                } else {
                    mCityTags.add(dataBean.getCityName());
                }
            }

        }
        return this;
    }


    @Override
    public int intLayoutId() {
        return R.layout.dialog_select_address1;
    }

    @Override
    public void convertView(BaseDialogViewHolder holder, BaseDialogFragment dialog) {
        setHeight(490);
        llBoxProvince = holder.getView(R.id.llBoxProvince);
        llBoxCity = holder.getView(R.id.llBoxCity);
        llBoxArea = holder.getView(R.id.llBoxArea);
        areaView = holder.getView(R.id.arae_view);
        mTagCloudView = holder.getView(R.id.tag_cloud);
        mTagCloudView.setLineCountLimit(4);
        mTagCloudView.setTags(mCityTags);
        mTagCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
            @Override
            public void onTagClick(int position) {
                Log.d("times", "--position=" + position);
                try {
                    mSelectedBeans.remove(position);
                    mCityTags.remove(position);
                    mTagCloudView.setTags(mCityTags);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        holder.getView(R.id.btn_clear).setOnClickListener((v) -> {
            try { //清空数据
                mCityTags.clear();
                mSelectedBeans.clear();
                mTagCloudView.setTags(mCityTags);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        holder.getView(R.id.btn_ok).setOnClickListener((v) -> {
            if (mCallback != null ) {
                try {
                    if (mFlag == null) {
                        mFlag = new View(getActivity());
                    }
                    //回调给省市区对象
                    mCallback.onCallback(mFlag,
                            mSelectedBeans);
                    dismiss();
                } catch (Exception e) {
                    dismiss();
                    e.printStackTrace();
                }
            } else {
//                ToastUtil.showToastString("请选择省市区");
                dismiss();
            }
        });
        if (isVisiable) {
            //  radioGroup.setVisibility(View.VISIBLE);
            areaView.setVisibility(View.VISIBLE);
        } else {
            areaView.setVisibility(View.GONE);
        }
        getProvince();
    }


    //省
    private SelectpRrovinceCityDlg setProvinceList(List<ShengListBean.DataBean> provinceList) {
        mProvinceList = provinceList;

        llBoxProvince.removeAllViews();
        for (int i = 0; i < mProvinceList.size(); i++) {
            View view = View.inflate(mContext, R.layout.item_select_address, null);
            TextView tv = view.findViewById(R.id.item_select_address_tv);
            tv.setText(mProvinceList.get(i).getProvince());
            mProvinceTvList.add(tv);
            int finalI = i;
            tv.setOnClickListener(v -> {
                for (int j = 0; j < mProvinceTvList.size(); j++) {
                    mProvinceTvList.get(j).setBackgroundColor(Color.parseColor("#FFFFFF"));
                    mProvinceTvList.get(j).setTextColor(Color.parseColor("#333333"));
                }
                mProvinceTvList.get(finalI).setBackgroundColor(Color.parseColor("#EEEEEE"));
                mProvinceTvList.get(finalI).setTextColor(Color.parseColor("#0F7CFF"));
                if (mProvinceCallback != null) {
                    mProvinceCallback.onProvince(finalI);
                }
                mCurrentProvinceBean = mProvinceList.get(finalI);
                try {
                    //初始化bean
                    QuListBean.DataBean allcityBean = new QuListBean.DataBean();
                    allcityBean.setCityCode(mCurrentProvinceBean.getProvinceCode());
                    allcityBean.setAreaCode(mCurrentProvinceBean.getProvinceCode());
                    allcityBean.setProvinceCode(mCurrentProvinceBean.getProvinceCode());
                    allcityBean.setArea(mCurrentProvinceBean.getProvince());
                    allcityBean.setCityName(mCurrentProvinceBean.getProvince());
                    allcityBean.setProvinceName(mCurrentProvinceBean.getProvince());
                    if (mSelectedBeans.size() > 0) {
                        boolean isAdded = false;
                        for (int j = 0; j < mSelectedBeans.size(); j++) {
                            if (mSelectedBeans.get(j).getArea().equals(allcityBean.getArea())) {
                                isAdded = true;
                            }
                        }
                        if (!isAdded) {
                            if (mCityTags.size() < CITY_TAGS_MAX_INT) {
                                Iterator<QuListBean.DataBean> iterator = mSelectedBeans.iterator();
                                while (iterator.hasNext()) {
                                    QuListBean.DataBean dataBean = iterator.next();
                                    if (dataBean.getProvinceCode().equals(mCurrentProvinceBean.getProvinceCode())) {
                                        mCityTags.remove(dataBean.getArea());
                                        iterator.remove();
                                    }
                                }
                                mSelectedBeans.add(allcityBean);
                                mCityTags.add(mCurrentProvinceBean.getProvince());
                                mTagCloudView.setTags(mCityTags);
                            } else {
                                ToastUtil.showToastString("最多选择6个，请先删除");
                            }
                        }
                    } else { //没有
                        if (mCityTags.size() < CITY_TAGS_MAX_INT) {
                            Iterator<QuListBean.DataBean> iterator = mSelectedBeans.iterator();
                            while (iterator.hasNext()) {
                                QuListBean.DataBean dataBean = iterator.next();
                                if (dataBean.getProvinceCode().equals(mCurrentProvinceBean.getProvinceCode())) {
                                    mCityTags.remove(dataBean.getArea());
                                    iterator.remove();
                                }
                            }
                            mSelectedBeans.add(allcityBean);
                            mCityTags.add(mCurrentProvinceBean.getProvince());
                            mTagCloudView.setTags(mCityTags);
                        } else {
                            ToastUtil.showToastString("最多选择6个，请先删除");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                llBoxCity.removeAllViews();
                llBoxArea.removeAllViews();
                mCityList.clear();
                mAreaList.clear();
                mCityTvList.clear();
                mAreaTvList.clear();
            });
            llBoxProvince.addView(view);
        }
        return this;
    }

    //市
    private SelectpRrovinceCityDlg setCityList(List<ShiListBean.DataBean> cityList) {
        mCityList = cityList;
//        if (mCityList == null){
//            ToastUtil.showShort("空的");
//        }
        int index = -1;
        for (int i = 0; i < mCityList.size(); i++) {
            ShiListBean.DataBean dataBean = mCityList.get(i);
            if (dataBean.getCity().equals("潍坊市")) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            ShiListBean.DataBean firstDataBean = mCityList.get(index);
            mCityList.remove(index);
            mCityList.add(0, firstDataBean);
        }

        llBoxCity.removeAllViews();
        for (int i = 0; i < mCityList.size(); i++) {
            View view = View.inflate(mContext, R.layout.item_select_address, null);
            TextView tv = view.findViewById(R.id.item_select_address_tv);
            tv.setText(mCityList.get(i).getCity());
            mCityTvList.add(tv);
            int finalI = i;
            tv.setOnClickListener(v -> {
                for (int j = 0; j < mCityTvList.size(); j++) {
                    mCityTvList.get(j).setBackgroundColor(Color.parseColor("#FFFFFF"));
                    mCityTvList.get(j).setTextColor(Color.parseColor("#333333"));
                }
                mCityTvList.get(finalI).setBackgroundColor(Color.parseColor("#EEEEEE"));
                mCityTvList.get(finalI).setTextColor(Color.parseColor("#0F7CFF"));
                if (mCityCallback != null) {
                    mCityCallback.onProvince(finalI);
                }
                mCurrentCityBean = mCityList.get(finalI);

                try {
                    QuListBean.DataBean allcityBean = new QuListBean.DataBean();
                    //区、市的code相同为市级
                    allcityBean.setCityCode(mCurrentCityBean.getCityCode());
                    allcityBean.setAreaCode(mCurrentCityBean.getCityCode());
                    allcityBean.setArea(mCurrentCityBean.getCity());
                    allcityBean.setCityName(mCurrentCityBean.getCity());
                    allcityBean.setProvinceName(mCurrentProvinceBean.getProvince());
                    allcityBean.setProvinceCode(mCurrentCityBean.getProvinceCode());
                    //选择的全市时候获取全省名字
                    if (mSelectedBeans.size() > 0) {
                        try {
                            boolean isAdded = false;
                            for (int j = 0; j < mSelectedBeans.size(); j++) {
                                if (mSelectedBeans.get(j).getArea().equals(allcityBean.getArea())) {
                                    isAdded = true;
                                }
                            }
                            if (!isAdded) {
                                if (mCityTags.size() < CITY_TAGS_MAX_INT) {
                                    Iterator<QuListBean.DataBean> iterator = mSelectedBeans.iterator();
                                    while (iterator.hasNext()) {
                                        QuListBean.DataBean dataBean = iterator.next();
                                        if (dataBean.getCityCode().equals(mCurrentCityBean.getCityCode())) {
                                            mCityTags.remove(dataBean.getArea());
                                            iterator.remove();
                                        }
                                        if (dataBean.getProvinceCode().equals(dataBean.getCityCode()) && dataBean.getProvinceCode().equals(mCurrentCityBean.getProvinceCode())) {
                                            mCityTags.remove(dataBean.getArea());
                                            iterator.remove();
                                        }
                                    }
                                    mSelectedBeans.add(allcityBean);
                                    mCityTags.add(mCurrentCityBean.getCity());
                                    mTagCloudView.setTags(mCityTags);
                                } else {
                                    ToastUtil.showToastString("最多选择6个，请先删除");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (mCityTags.size() < CITY_TAGS_MAX_INT) {
                            Iterator<QuListBean.DataBean> iterator = mSelectedBeans.iterator();
                            while (iterator.hasNext()) {
                                QuListBean.DataBean dataBean = iterator.next();
                                if (dataBean.getCityCode().equals(mCurrentCityBean.getCityCode())) {
                                    mCityTags.remove(dataBean.getArea());
                                    iterator.remove();
                                }
                                if (dataBean.getProvinceCode().equals(dataBean.getCityCode()) && dataBean.getProvinceCode().equals(mCurrentCityBean.getProvinceCode())) {
                                    mCityTags.remove(dataBean.getArea());
                                    iterator.remove();
                                }
                            }
                            mSelectedBeans.add(allcityBean);
                            mCityTags.add(mCurrentCityBean.getCity());
                            mTagCloudView.setTags(mCityTags);
                        } else {
                            ToastUtil.showToastString("最多选择6个，请先删除");
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mAreaList.clear();
                mAreaTvList.clear();
            });
            llBoxCity.addView(view);
        }
        return this;
    }

    //县、区
    private SelectpRrovinceCityDlg setAreaList(List<QuListBean.DataBean> areaList) {
        mAreaList = areaList;
        int index = -1;
        for (int i = 0; i < mAreaList.size(); i++) {
            QuListBean.DataBean dataBean = mAreaList.get(i);
            if (dataBean.getArea().equals("青州市")) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            QuListBean.DataBean firstDataBean = mAreaList.get(index);
            mAreaList.remove(index);
            mAreaList.add(0, firstDataBean);
        }
        //新增一个全市
        if (mAreaList.size() > 0) {
//            QuListBean.DataBean allcityBean = new QuListBean.DataBean();
//            allcityBean.setCityCode(mAreaList.get(0).getCityCode());
//            allcityBean.setAreaCode(mAreaList.get(0).getCityCode());
//            allcityBean.setArea(mAreaList.get(0).getCityName());
//            allcityBean.setCityName(mAreaList.get(0).getCityName());
//
//            //选择的全市时候获取全省名字
//            if (mSelectedBeans.size()>0) {
//                try {
//                    boolean isAdded = false;
//                    for(int i=0;i< mSelectedBeans.size();i++){
//                        if(mSelectedBeans.get(i).getArea().equals(allcityBean.getArea())){
//                            isAdded =true;
//                        }
//                    }
//                    if(!isAdded) {
//                        mSelectedBeans.add(allcityBean);
//                        mCityTags.add(mCurrentCityBean.getCity());
//                        mTagCloudView.setTags(mCityTags);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            //刷新界面

//            mAreaList.add(0, allcityBean);
        }

        llBoxArea.removeAllViews();
        for (int i = 0; i < mAreaList.size(); i++) {
            View view = View.inflate(mContext, R.layout.item_select_address, null);
            TextView tv = view.findViewById(R.id.item_select_address_tv);
            tv.setText(mAreaList.get(i).getArea());
            mAreaTvList.add(tv);
            int finalI = i;
            tv.setOnClickListener(v -> {
                for (int j = 0; j < mAreaTvList.size(); j++) {
                    mAreaTvList.get(j).setBackgroundColor(Color.parseColor("#FFFFFF"));
                    mAreaTvList.get(j).setTextColor(Color.parseColor("#333333"));
                }
                mAreaTvList.get(finalI).setBackgroundColor(Color.parseColor("#EEEEEE"));
                mAreaTvList.get(finalI).setTextColor(Color.parseColor("#0F7CFF"));
                mCurrentAreaBean = mAreaList.get(finalI);
                //新增区
                if (mTagCloudView != null) {
                    try {
                        if (mCityTags.size() >= 6) {
                            ToastUtil.showToastString("最多选择6个，请先删除");
                            return;
                        }
                        //遍历选中列表 ，选中全市时候把区给清空
                        if (mSelectedBeans.size() > 0) {
                            //如果已经选过后直接return
                            for (int t = 0; t < mSelectedBeans.size(); t++) {
                                if (mSelectedBeans.get(t).getAreaCode().equals(mCurrentAreaBean.getAreaCode())) {
                                    return;
                                }
                            }

                            if (!mCurrentAreaBean.getCityCode().equals(mCurrentAreaBean.getAreaCode())) {
                                //选择某区时候把全市删除
                                Iterator<QuListBean.DataBean> iterator = mSelectedBeans.iterator();
                                while (iterator.hasNext()) {
                                    QuListBean.DataBean dataBean = iterator.next();
                                    if (dataBean.getAreaCode().equals(mCurrentAreaBean.getCityCode()) || dataBean.getAreaCode().equals(mCurrentAreaBean.getProvinceCode())) {
                                        mCityTags.remove(mCurrentCityBean.getCity());
                                        iterator.remove();
                                    }
                                }
                            }

                        }
                        mCurrentAreaBean.setCityName(mCurrentCityBean.getCity());
                        mCurrentAreaBean.setProvinceName(mCurrentProvinceBean.getProvince());
                        mSelectedBeans.add(mCurrentAreaBean);
                        //选择的全市时候获取全省名字
                        if (mCurrentAreaBean.getCityCode().equals(mCurrentAreaBean.getAreaCode())) {
                            mCityTags.add(mCurrentCityBean.getCity());
                        } else {
                            mCityTags.add(mCurrentAreaBean.getArea());
                        }
                        //刷新界面
                        mTagCloudView.setTags(mCityTags);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            llBoxArea.addView(view);
        }
        return this;
    }


    /**
     * 获取省
     */
    private void getProvince() {

        //调用接口获取数据
//        UploadAnimDialogUtils.singletonDialogUtils().showCustomProgressDialog(getActivity(), "获取数据");
        RequestEntity requestEntity = new RequestEntity(0);
        requestEntity.setData(new Object());
        String jsonString = JsonManager.createJsonString(requestEntity);
        OkgoUtils.post(ProjectUrl.REGION_GETPROVINCE, jsonString).execute(new MyStringCallback<ShengListBean>() {
            @Override
            public void onResponse(ShengListBean resp) {
//                UploadAnimDialogUtils.singletonDialogUtils().deleteCustomProgressDialog();
                if (resp.isSuccess()) {
                    List<ShengListBean.DataBean> data = resp.getData();
                    for (int i = 0; i < data.size(); i++) {
                        ShengListBean.DataBean dataBean = data.get(i);
                        if (dataBean.getId().equals("15")) {
                            data.remove(dataBean);
                        }
                    }
                    ShengListBean.DataBean dataBean = new ShengListBean.DataBean();
                    dataBean.setId("15");
                    dataBean.setProvinceCode("370000");
                    dataBean.setProvince("山东");
                    data.add(0, dataBean);
                    setProvinceList(resp.getData());
                    setProvinceCallback(position -> {
                        //获取省
                        getCity(resp.getData().get(position).getProvinceCode());
                    });
                }
            }

            @Override
            public Class<ShengListBean> getClazz() {
                return ShengListBean.class;
            }
        });

    }

    /**
     * 获取市
     */
    private void getCity(String provinceCode) {


        //调用接口获取数据
//        UploadAnimDialogUtils.singletonDialogUtils().showCustomProgressDialog(getActivity(), "获取数据");
        RequestEntity requestEntity = new RequestEntity(0);
        HashMap<String, String> data = new HashMap<>();
        data.put("provinceCode", provinceCode);
        requestEntity.setData(data);
        String jsonString = JsonManager.createJsonString(requestEntity);
        OkgoUtils.post(ProjectUrl.REGION_GETCITY, jsonString).execute(new MyStringCallback<ShiListBean>() {
            @Override
            public void onResponse(ShiListBean resp) {
//                UploadAnimDialogUtils.singletonDialogUtils().deleteCustomProgressDialog();
                if (resp.isSuccess()) {
                    setCityList(resp.getData());
                    setCityCallback(position -> {
                        //获取市
                        getArea(resp.getData().get(position).getCityCode());
                    });
                }
            }

            @Override
            public Class<ShiListBean> getClazz() {
                return ShiListBean.class;
            }
        });
    }

    /**
     * 获取区
     */
    private void getArea(String cityCode) {


        //调用接口获取数据
//        UploadAnimDialogUtils.singletonDialogUtils().showCustomProgressDialog(getActivity(), "获取数据");
        RequestEntity requestEntity = new RequestEntity(0);
        HashMap<String, String> data = new HashMap<>();
        data.put("cityCode", cityCode);
        requestEntity.setData(data);
        String jsonString = JsonManager.createJsonString(requestEntity);
        OkgoUtils.post(ProjectUrl.REGION_GETAREA, jsonString).execute(new MyStringCallback<QuListBean>() {
            @Override
            public void onResponse(QuListBean resp) {
//                UploadAnimDialogUtils.singletonDialogUtils().deleteCustomProgressDialog();
                if (resp.isSuccess()) {
                    setAreaList(resp.getData());
                }
            }

            @Override
            public Class<QuListBean> getClazz() {
                return QuListBean.class;
            }
        });
    }

    ProvinceCallback mProvinceCallback;

    public interface ProvinceCallback {
        void onProvince(int potion);
    }

    public SelectpRrovinceCityDlg setProvinceCallback(ProvinceCallback callback) {
        mProvinceCallback = callback;
        return this;
    }


    CityCallback mCityCallback;

    public interface CityCallback {
        void onProvince(int position);
    }

    public SelectpRrovinceCityDlg setCityCallback(CityCallback callback) {
        mCityCallback = callback;
        return this;
    }


    AreaCallback mAreaCallback;

    public interface AreaCallback {
        void onProvince(int position);
    }

    public void setAreaCallback(AreaCallback callback) {
        mAreaCallback = callback;
    }


    Callback mCallback;

    public interface Callback {
        void onCallback(View flag,  ArrayList<QuListBean.DataBean> areaBeans);
    }

    public SelectpRrovinceCityDlg setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

}
