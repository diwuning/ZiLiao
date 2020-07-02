package com.nmpa.nmpaapp.apply;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.constants.WebApi;
import com.nmpa.nmpaapp.http.OkHttpUtil;
import com.nmpa.nmpaapp.utils.RxBus;
import com.nmpa.nmpaapp.widget.NestedExpandListView;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import rx.functions.Action1;

/*
* 申请类型
* 两级树状图
* */
public class TypeDialog extends Dialog {
    private static final String TAG = "TypeDialog";
    private Button btn_cancel,btn_confirm;
    private List<TypeBean> typeBeanList = new ArrayList<>();
    private NestedExpandListView nelv_type;
    private Context mContext;
    private TypeBean selectType;

    public TypeDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    
    public TypeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected TypeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apply_type_dialog);

        getWindow().setGravity(Gravity.CENTER); //显示在
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth()/3*2; //设置dialog的宽度为当前手机屏幕的宽度
        p.height = d.getHeight()/3*2;
        getWindow().setAttributes(p);

//        //按空白处不能取消动画
//        setCanceledOnTouchOutside(false);
        btn_cancel = findViewById(R.id.btn_negative_custom_dialog);
        btn_confirm = findViewById(R.id.btn_positive_custom_dialog);
        nelv_type = findViewById(R.id.nelv_type);
        //去掉ExpandableListView 默认的箭头
        nelv_type.setGroupIndicator(null);
        getApplyTypeData();
        nelv_type.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                Log.d("TypeDialog","onGroupClick   "+i+","+(LinearLayout)expandableListView.getChildAt(0)+","+expandableListView.getChildCount());
                selectType = typeBeanList.get(i);
                for (int j = 0;j<expandableListView.getChildCount();j++) {
                    if ((((LinearLayout)expandableListView.getChildAt(j)).getChildAt(1)) instanceof LinearLayout) {
                        ((TextView)((LinearLayout)((LinearLayout)expandableListView.getChildAt(j)).getChildAt(1)).getChildAt(1)).setSelected(false);
                    }
                }
                LinearLayout linearLayout = (LinearLayout) view;
                ((TextView)((LinearLayout)linearLayout.getChildAt(1)).getChildAt(1)).setSelected(true);
                if (expandableListView.isGroupExpanded(i)) {
                    ((ImageView)((LinearLayout)linearLayout.getChildAt(1)).getChildAt(0)).setImageResource(R.drawable.tree_econpand);
                } else {
                    ((ImageView)((LinearLayout)linearLayout.getChildAt(1)).getChildAt(0)).setImageResource(R.drawable.tree_expand);
                }
                return false;
            }
        });

        initEvent();
        RxBus.getDefault().toObservable(TypeBean.class).subscribe(new Action1<TypeBean>() {
            @Override
            public void call(TypeBean typeBean) {
                selectType = typeBean;
            }
        });
    }
    
    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( onClickBottomListener!= null) {
                    onClickBottomListener.onPositiveClick(selectType);
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( onClickBottomListener!= null) {
                    onClickBottomListener.onNegtiveClick();
                }
            }
        });
    }
    
    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;
    public TypeDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }
    public interface OnClickBottomListener{
        /**
         * 点击确定按钮事件
         */
         public void onPositiveClick(TypeBean typeBean);
        /**
         * 点击取消按钮事件
         */
        public void onNegtiveClick();
    }

    private void getApplyTypeData() {
        OkHttpUtil.get(TAG, WebApi.ACTIVITY_APPLY_SEARCH, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.d(TAG,"getApplyData  e="+e);
            }
            
            @Override
            public void onResponse(String response, int id) {
                Log.d(TAG,"getApplyData  response="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    if ((int)object.get("code") == 200) {
                        JSONArray array = object.getJSONArray("data");
                        Gson gson = new Gson();
                        for (int i=0;i<array.length();i++) {
                          TypeBean typeBean = gson.fromJson(array.get(i).toString(),TypeBean.class);
                            typeBeanList.add(typeBean);
                        }
                        DialogListAdapter dialogListAdapter = new DialogListAdapter(mContext,typeBeanList);
                        nelv_type.setAdapter(dialogListAdapter);
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }
}
