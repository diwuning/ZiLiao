package com.nmpa.nmpaapp.modules.employees;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.constants.WebApi;
import com.nmpa.nmpaapp.http.OkHttpUtil;
import com.nmpa.nmpaapp.modules.office.adapter.TypeTreeRecyclerAdapter;
import com.nmpa.nmpaapp.modules.office.bean.TypeTreeBean;
import com.nmpa.nmpaapp.modules.tree.Node;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class OfficeTypeDialog extends Dialog implements View.OnClickListener {
    private static final String TAG = "OfficeTypeDialog";
    private Context mContext;
    private RecyclerView rv_typeTree;
    private TypeTreeRecyclerAdapter typeTreeRecyclerAdapter;
    private TypeTreeBean selectedBean;
    private Button btn_confirm,btn_cancel;
    private String source;
    private String orgId;

    public OfficeTypeDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    
    public OfficeTypeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    public OfficeTypeDialog(@NonNull Context context, int themeResId,String source) {
        super(context, themeResId);
        mContext = context;
        this.source = source;
    }
    
    public OfficeTypeDialog(@NonNull Context context, int themeResId,String source,String orgId) {
        super(context, themeResId);
        mContext = context;
        this.source = source;
        this.orgId = orgId;
    }

    protected OfficeTypeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_office_type);

        getWindow().setGravity(Gravity.CENTER); //显示在
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth()/5*4; //设置dialog的宽度为当前手机屏幕的宽度
        p.height = d.getHeight()/5*4;
        getWindow().setAttributes(p);

        rv_typeTree = findViewById(R.id.rv_typeTree);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_confirm.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);

        getOfficeType();
    }
    
    List<Node> mDatas = new ArrayList<>();
    private List<TypeTreeBean> typeTreeBeans = new ArrayList<>();
    private void getOfficeType() {
        String url = "";
        if (source.equals("officeType")) {
            url = WebApi.OFFICE_TYPE;
        } else if(source.equals("area")) {
            url = WebApi.OFFICE_AREA;
        } else if(source.equals("nmpa")) {
            url = WebApi.OFFICE_NMPA;
        } else if(source.equals("nmpaUser")) {
            if (orgId != null) {
                url = WebApi.OFFICE_NMPA_USER+"?orgId="+orgId;
            }
        }
        OkHttpUtil.get(TAG, url, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG,"getOfficeType e="+e);
            }
            
            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG,"getOfficeType response="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getInt("code") == 200) {
                        JSONArray array = object.getJSONArray("data");
                        Gson gson = new Gson();
                        for (int i = 0;i<array.length();i++) {
                          TypeTreeBean typeTreeBean = gson.fromJson(array.get(i).toString(), TypeTreeBean.class);
                            typeTreeBeans.add(typeTreeBean);
                        }

                        for (int j=0;j<typeTreeBeans.size();j++) {
                            TypeTreeBean typeTreeBean = typeTreeBeans.get(j);
                            mDatas.add(new Node(typeTreeBean.getId(),0,typeTreeBean.getName(),typeTreeBean));
                            if (typeTreeBean.getChildren() != null && typeTreeBean.getChildren().size() != 0) {
                                List<TypeTreeBean> typeTreeBeans1 = typeTreeBean.getChildren();
                                for (int j1 = 0;j1<typeTreeBeans1.size();j1++) {
                                    TypeTreeBean typeTreeBean1 = typeTreeBeans1.get(j1);
                                    mDatas.add(new Node(typeTreeBean1.getId(),typeTreeBean.getId(),typeTreeBean1.getName(),typeTreeBean1));
                                    if (typeTreeBean1.getChildren() != null && typeTreeBean1.getChildren().size() != 0) {
                                        List<TypeTreeBean> typeTreeBeans2 = typeTreeBean1.getChildren();
                                        for (int j2 = 0;j2<typeTreeBeans2.size();j2++) {
                                            TypeTreeBean typeTreeBean2 = typeTreeBeans2.get(j2);
                                            mDatas.add(new Node(typeTreeBean2.getId(),typeTreeBean1.getId(),typeTreeBean2.getName(),typeTreeBean2));
                                            if (typeTreeBean2.getChildren() != null && typeTreeBean2.getChildren().size() != 0) {
                                                List<TypeTreeBean> typeTreeBeans3 = typeTreeBean2.getChildren();
                                                for (int j3 = 0;j3<typeTreeBeans3.size();j3++) {
                                                  TypeTreeBean typeTreeBean3 = typeTreeBeans3.get(j3);
                                                    mDatas.add(new Node(typeTreeBean3.getId(),typeTreeBean2.getId(),typeTreeBean3.getName(),typeTreeBean3));
                                                    if (typeTreeBean3.getChildren() != null && typeTreeBean3.getChildren().size() != 0) {
                                                        List<TypeTreeBean> typeTreeBeans4 = typeTreeBean3.getChildren();
                                                        for (int j4 = 0;j4<typeTreeBeans4.size();j4++) {
                                                            TypeTreeBean typeTreeBean4 = typeTreeBeans4.get(j4);
                                                            mDatas.add(new Node(typeTreeBean4.getId(),typeTreeBean3.getId(),typeTreeBean4.getName(),typeTreeBean4));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        initTypeList();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void initTypeList() {
        rv_typeTree.setLayoutManager(new LinearLayoutManager(mContext));
        typeTreeRecyclerAdapter = new TypeTreeRecyclerAdapter(rv_typeTree, mContext,
                mDatas, 1,R.drawable.tree_expand,R.drawable.tree_econpand, true);
        rv_typeTree.setAdapter(typeTreeRecyclerAdapter);
        typeTreeRecyclerAdapter.setOnItemClickListener(new TypeTreeRecyclerAdapter.onAItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.e(TAG,"position="+position);
                updatePosition(position);
                clickShow();
            }
        });
    }
    
    /**
     * 更新选择窗
     *
     * @param selectedPosition 选中位置
     */
    public void updatePosition(int selectedPosition) {
        if (typeTreeRecyclerAdapter != null) {
            typeTreeRecyclerAdapter.setSelectedPosition(selectedPosition);
            typeTreeRecyclerAdapter.notifyDataSetChanged();
        }
    }
    
    /**
     * 显示选中数据
     */
    public void clickShow(){
//        StringBuilder sb = new StringBuilder();
        List<TypeTreeBean> typeTreeBeanList = new ArrayList<>();
        final List<Node> allNodes = typeTreeRecyclerAdapter.getAllNodes();
        for (int i = 0; i < allNodes.size(); i++) {
            if (allNodes.get(i).isChecked()){
                //                sb.append(allNodes.get(i).getName()+",");
                typeTreeBeanList.add((TypeTreeBean) allNodes.get(i).bean);
            }
        }
//        String strNodesName = sb.toString();
        if (typeTreeBeanList != null && typeTreeBeanList.size() != 0) {
            Log.e(TAG,"bean="+typeTreeBeanList.get(0).getName());
            selectedBean = typeTreeBeanList.get(0);
        }
    }
    
    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;
    public void setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
    }
    public interface OnClickBottomListener{
        /**
         * 点击确定按钮事件
         */
        public void onPositiveClick(TypeTreeBean typeBean);
        /**
         * 点击取消按钮事件
         */
        public void onNegtiveClick();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                if ( onClickBottomListener!= null) {
                    onClickBottomListener.onPositiveClick(selectedBean);
                }
                break;
            case R.id.btn_cancel:
                if ( onClickBottomListener!= null) {
                    onClickBottomListener.onNegtiveClick();
                }
                break;
        }
    }
}
