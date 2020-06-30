package com.nmpa.nmpaapp.modules.punish;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.gson.Gson;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.base.BaseActivity;
import com.nmpa.nmpaapp.base.ui.AppBarConfig;
import com.nmpa.nmpaapp.base.ui.StatusBarUtil;
import com.nmpa.nmpaapp.constants.WebApi;
import com.nmpa.nmpaapp.http.OkHttpUtil;
import com.nmpa.nmpaapp.http.WebFrontUtil;
import com.nmpa.nmpaapp.modules.punish.bean.PunishBean;
import com.nmpa.nmpaapp.modules.punish.bean.PunishWayBean;
import com.nmpa.nmpaapp.modules.punish.bean.ReasonBean;
import com.nmpa.nmpaapp.modules.punish.bean.ShopBean;
import com.nmpa.nmpaapp.router.Page;
import com.nmpa.nmpaapp.utils.ColView;
import com.nmpa.nmpaapp.utils.StringUtils;
import com.nmpa.nmpaapp.utils.ToastUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;

@Route(path = Page.ACTIVITY_PUNISH_DETAIL)
public class PunishUpdateActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "PunishUpdateActivity";
    private Context mContext;
    @BindView(R.id.spinner_name)
    TextView tv_shopName;
    @BindView(R.id.tv_punishOwner)
    TextView tv_legalPerson;
    @BindView(R.id.tv_punishNumber)
    EditText tv_punishNo;
    @BindView(R.id.spinner_mode)
    Spinner spinner_punishWay;
    @BindView(R.id.tv_reconsider)
    EditText tv_reviewAgency;
    @BindView(R.id.tv_start)
    TextView tv_start;
    @BindView(R.id.tv_end)
    TextView tv_end;
    @BindView(R.id.spinner_breakMode)
    Spinner spinner_reason;
    @BindView(R.id.et_reason)
    EditText et_reason;
    @BindView(R.id.spinner_breakAction)
    Spinner spinner_evidence;
    @BindView(R.id.et_evidence)
    EditText et_evidence;
    @BindView(R.id.spinner_breakEvidence)
    Spinner spinner_lawType;
    @BindView(R.id.et_lawType)
    EditText et_lawType;
    @BindView(R.id.spinner_breakSet)
    Spinner spinner_lawFix;
    @BindView(R.id.et_lawFix)
    EditText et_lawFix;
    @BindView(R.id.spinner_breakCorrect)
    Spinner spinner_lawAmend;
    @BindView(R.id.et_lawAmend)
    EditText et_lawAmend;
    @BindView(R.id.tv_breakProvision)
    EditText tv_lawItem;
    @BindView(R.id.tv_breakDesc)
    EditText tv_breakDesc;
    PunishBean punishBean;
    @BindView(R.id.btn_update)
    Button btn_update;
    @BindView(R.id.btn_del)
    Button btn_del;

    @BindView(R.id.tv_punishTitle)
    EditText tv_punishTitle;

    @BindView(R.id.view_apply)
    View view_apply;
    @BindView(R.id.ll_apply)
    LinearLayout ll_apply;
    @BindView(R.id.tv_applyType)
    TextView tv_applyType;
    @BindView(R.id.rl_type)
    RelativeLayout rl_type;
    @BindView(R.id.tl_operate)
    TableLayout tl_operate;
    @BindView(R.id.ll_total)
    LinearLayout ll_total;

    @BindView(R.id.tv_punishTel)
    EditText tv_punishTel;
    private int mYear, mMonth, mDay;
    String startTime;
    String endTime;
    private ShopBean shopBean;
    private String punishWay;
    private String inspectId;
    private String from;

    //联动下拉列表
    private List<PunishWayBean> wayBeans = new ArrayList<>();
    private String[] modeArr;
    private List<ReasonBean> reasonBeans = new ArrayList<>();//违法类型
    private String[] reasonArr;
    private List<ReasonBean> evidenceBeans = new ArrayList<>();//违法行为
    private String[] evidenceArr;
    private List<ReasonBean> lawTypeBeans = new ArrayList<>();//违法证据
    private String[] lawTypeArr;
    private List<ReasonBean> lawFixBeans = new ArrayList<>();//违法规定
    private String[] lawFixArr;
    private List<ReasonBean> lawAmendBeans = new ArrayList<>();//违法规定
    private String[] lawAmendArr;

    //    String[] modeArr = {"","警告","罚款","没收违法所得","责令立即整改","责令限期整改"};
//    String[][] breakModeArr = {
//            {""},
//            {"","警告类型1","警告类型2"},
//            {"","罚款类型1","罚款类型2","罚款类型3"},
//            {"","没收违法所得类型1","没收违法所得类型2","没收违法所得类型3"},
//            {"","责令立即整改类型1","责令立即整改类型2","责令立即整改类型3"},
//            {""}
//    };
//    String[][][] actionArr = {
//            {{""}},
//            {{""},{"","警告类型1行为1","警告类型1行为2"},{"","警告类型2行为1","警告类型2行为2","警告类型2行为3"}},
//            {{""},{"","罚款类型1行为1","罚款类型1行为2"},{"","罚款类型2行为1","罚款类型2行为2","罚款类型2行为3"}},
//            {{""},{"","没收违法所得类型1行为1","没收违法所得类型1行为2"},{"","没收违法所得类型2行为1","没收违法所得类型2行为2","没收违法所得类型2行为3"},{""}},
//            {{""},{""},{""},{""}},
//            {{""}}
//    };

    boolean isSetData = true;
    @Override
    public void onBeforeSetContentView() {
        StatusBarUtil.translucent(this, ContextCompat.getColor(this, R.color.color_0a5fb6));
    }

    @Override
    public int getLayoutResID() {
        return R.layout.activity_punish_update;
    }

    @Override
    protected CharSequence setActionBarTitle() {
      if (!StringUtils.isEmpty(getIntent().getStringExtra("from")) && getIntent().getStringExtra("from").equals("disable")) {
            return "行政处罚管理－查看";
        }
        return "行政处罚管理－修改";
    }
    @Nullable
    @Override
    public AppBarConfig getAppBarConfig() {
        return mAppBarCompat;
    }

    @Override
    public void initContentView(@Nullable Bundle savedInstanceState) {
      mContext = PunishUpdateActivity.this;
        ButterKnife.bind(this);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            punishBean = (PunishBean) bundle.get("punishDetail");
            Log.d(TAG,punishBean.toString());
        }
        if (!StringUtils.isEmpty(getIntent().getStringExtra("from"))) {
          from = getIntent().getStringExtra("from");
            if (from.equals("disable")) {
                tl_operate.setVisibility(View.GONE);
                ViewGroup contentView = (ViewGroup)
                        this.getWindow().findViewById(R.id.ll_total);
                ColView colView = new ColView();
                colView.setDisAbleMore(contentView,true);
                tv_start.setEnabled(false);
                tv_start.setOnClickListener(null);
                tv_end.setEnabled(false);
                tv_end.setOnClickListener(null);
            }
        }
        initWayData();
        getReasonData("1","");
        initView();
        btn_update.setOnClickListener(this);
        btn_del.setOnClickListener(this);
        rl_type.setOnClickListener(this);
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (punishBean != null) {
            tv_shopName.setText(punishBean.getOfficeName());
            tv_legalPerson.setText(punishBean.getLegalPerson());
            tv_punishNo.setText(punishBean.getPunishNo());
            tv_punishTitle.setText(punishBean.getPunishTitle());
            tv_reviewAgency.setText(punishBean.getReviewAgency());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
            Log.e(TAG,"startTime="+punishBean.getStartTime());
            startTime = punishBean.getStartTime();
            tv_start.setText(startTime);
            if (punishBean.getEndTime() != null) {
//                endTime = dateFormat.format(Long.valueOf(punishBean.getEndTime()));
                endTime = punishBean.getEndTime();
                tv_end.setText(endTime);
            }
            //获取违法行为
            getReasonData("2",punishBean.getReasonId());
            //获取违法证据
            getReasonData("5",punishBean.getEvidenceId());
            //获取违法规定
            getReasonData("3",punishBean.getEvidenceId());
            //获取违法改正
            getReasonData("4",punishBean.getEvidenceId());
            tv_lawItem.setText(punishBean.getLawItem());
            tv_breakDesc.setText(punishBean.getRemarks());
            tv_punishTel.setText(punishBean.getPunishTel());
            inspectId = punishBean.getInspectId();
            tv_applyType.setText(punishBean.getApplyType());
            spinner_punishWay.setOnItemSelectedListener(this);
            //违法类型
            spinner_reason.setOnItemSelectedListener(this);
            spinner_evidence.setOnItemSelectedListener(this);
            spinner_lawType.setOnItemSelectedListener(this);
            spinner_lawFix.setOnItemSelectedListener(this);
            spinner_lawAmend.setOnItemSelectedListener(this);
        }
    }

    int wayIndex = 0;
    private void initWayData() {
        //处罚方式
        HashMap<String, Object> baseParam = WebFrontUtil.getBaseParam();
        baseParam.put("type","punishWay");
        OkHttpUtil.post(TAG, WebApi.PUNISH_WAY_URL, baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                Log.d(TAG,"response="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    if ((int)object.get("code") == 200) {
                      JSONArray array = object.getJSONArray("data");
                        modeArr= new String[array.length()];
//                        modeArr[0] = "";
                        for (int i=0;i<array.length();i++) {
                            PunishWayBean wayBean = new Gson().fromJson(array.getJSONObject(i).toString(),PunishWayBean.class);
                            wayBeans.add(wayBean);
                            modeArr[i] = wayBean.getLabel();
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter(mContext,R.layout.punish_spinner_item,modeArr);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_punishWay.setAdapter(adapter);
                        Log.d(TAG,"punishBean.getPunishWay()="+punishBean.getPunishWay());
                        for (int i=0;i<modeArr.length;i++) {
                          if (modeArr[i].equals(punishBean.getPunishWay())) {
                                if (punishBean.getPunishWay().contains("整改")) {
                                    punishWay = "整改";
                                }
                                if (punishBean.getPunishWay().contains("整顿")) {
                                    punishWay = "整顿";
                                }
                                wayIndex = i;
                            }
                        }
                        spinner_punishWay.setSelection(wayIndex);
                        punishBean.setPunishWay(String.valueOf(wayIndex+1));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void getReasonData(String punishType,String parentId) {
        HashMap<String, Object> baseParam = WebFrontUtil.getBaseParam();
        baseParam.put("punishType",punishType);
        baseParam.put("parentId",parentId);
        baseParam.put("punishName","");
        OkHttpUtil.post(TAG, WebApi.PUNISH_REASON_URL, baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.d(TAG,"reason onError="+e);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d(TAG,"reason response="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    if ((int)object.get("code") == 200) {
                        JSONArray array = object.getJSONArray("data");
                        if (punishType.equals("1")) {
                            setReasonAdapter(array);
                        } else if (punishType.equals("2")) {
                            setEvidenceAdapter(array);
                        } else if (punishType.equals("5")) {
                            setLawTypeAdapter(array);
                        } else if (punishType.equals("3")) {
                            setLawFixAdapter(array);
                        } else if (punishType.equals("4")) {
                            setLawAmendAdapter(array);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void setReasonAdapter (JSONArray array) {
        reasonArr = new String[array.length()+1];
        reasonArr[0] = "";
        int reasonIndex = 0;

        try {
            for (int i= 0;i<array.length();i++) {
                ReasonBean reasonBean = new Gson().fromJson(array.getJSONObject(i).toString(),ReasonBean.class);
                reasonBeans.add(reasonBean);
                reasonArr[i+1] = reasonBean.getName();
                if (reasonBean.getName().equals(punishBean.getReason())) {
                    reasonIndex = i+1;
                }
            }
            ArrayAdapter<String> breakAdapter = new ArrayAdapter<String>(mContext,R.layout.punish_spinner_item,reasonArr);
            breakAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_reason.setAdapter(breakAdapter);
            Log.d(TAG,"setReasonAdapter  reason="+punishBean.getReason());
            if (isSetData) {
                spinner_reason.setSelection(reasonIndex);
                et_reason.setText(punishBean.getReason());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void setEvidenceAdapter(JSONArray array) {
        evidenceArr = new String[array.length()+1];
        evidenceArr[0] = "";
        int evidenceIndex = 0;
        evidenceBeans.clear();
        try {
            for (int i= 0;i<array.length();i++) {
                ReasonBean reasonBean = new Gson().fromJson(array.getJSONObject(i).toString(), ReasonBean.class);
                evidenceBeans.add(reasonBean);
                evidenceArr[i+1] = reasonBean.getName();
                if (reasonBean.getName().equals(punishBean.getEvidence())) {
                    evidenceIndex = i+1;
                }
            }
            ArrayAdapter<String> breakAdapter = new ArrayAdapter<String>(mContext,R.layout.punish_spinner_item,evidenceArr);
            breakAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_evidence.setAdapter(breakAdapter);
            Log.d(TAG,"setEvidenceAdapter  reason="+punishBean.getEvidence());
            if (isSetData) {
                spinner_evidence.setSelection(evidenceIndex);
                et_evidence.setText(punishBean.getEvidence());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void setLawTypeAdapter(JSONArray array) {
        lawTypeArr = new String[array.length()+1];
        lawTypeArr[0] = "";
        int layTypeIndex = 0;
        lawTypeBeans.clear();
        try {
            for (int i= 0;i<array.length();i++) {
                ReasonBean reasonBean = new Gson().fromJson(array.getJSONObject(i).toString(), ReasonBean.class);
                lawTypeBeans.add(reasonBean);
                lawTypeArr[i+1] = reasonBean.getName();
                if (reasonBean.getName().equals(punishBean.getLawType())) {
                    layTypeIndex = i+1;
                }
            }
            ArrayAdapter<String> breakAdapter = new ArrayAdapter<String>(mContext,R.layout.punish_spinner_item,lawTypeArr);
            breakAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_lawType.setAdapter(breakAdapter);
            Log.d(TAG,"setLawTypeAdapter  reason="+punishBean.getLawType());
            if (isSetData) {
                spinner_lawType.setSelection(layTypeIndex);
                et_lawType.setText(punishBean.getLawType());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void setLawFixAdapter(JSONArray array) {
        lawFixArr = new String[array.length()+1];
        lawFixArr[0] = "";
        int lawFixIndex = 0;
        lawFixBeans.clear();

        try {
            for (int i= 0;i<array.length();i++) {
                ReasonBean reasonBean = new Gson().fromJson(array.getJSONObject(i).toString(), ReasonBean.class);
                lawFixBeans.add(reasonBean);
                lawFixArr[i+1] = reasonBean.getName();

                if (reasonBean.getName().equals(punishBean.getLawFix())) {
                    lawFixIndex = i+1;
                }
            }
            ArrayAdapter<String> breakAdapter = new ArrayAdapter<String>(mContext,R.layout.punish_spinner_item,lawFixArr);
            breakAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_lawFix.setAdapter(breakAdapter);
            Log.d(TAG,"setLawFixAdapter  reason="+punishBean.getLawFix());
            if (isSetData) {
                spinner_lawFix.setSelection(lawFixIndex);
                et_lawFix.setText(punishBean.getLawFix());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    
    private void setLawAmendAdapter(JSONArray array) {
        lawAmendArr = new String[array.length()+1];
        lawAmendArr[0] = "";
        int lawAmendIndex = 0;
        lawAmendBeans.clear();

        try {
            for (int i= 0;i<array.length();i++) {
                ReasonBean reasonBean = new Gson().fromJson(array.getJSONObject(i).toString(), ReasonBean.class);
                lawAmendBeans.add(reasonBean);
                lawAmendArr[i+1] = reasonBean.getName();

                if (reasonBean.getName().equals(punishBean.getLawAmend())) {
                    lawAmendIndex = i+1;
                }
            }
            ArrayAdapter<String> breakAdapter = new ArrayAdapter<String>(mContext,R.layout.punish_spinner_item,lawAmendArr);
            breakAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_lawAmend.setAdapter(breakAdapter);
            Log.d(TAG,"setReasonAdapter  reason="+punishBean.getLawAmend());
            if (isSetData) {
                spinner_lawAmend.setSelection(lawAmendIndex);
                et_lawAmend.setText(punishBean.getLawAmend());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    
    private void initView(){
        tv_shopName.setOnClickListener(this);
        Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
        Log.d(TAG,"mYear="+mYear+",mMonth="+mMonth+",mDay="+mDay);
        tv_start.setOnClickListener(this);
        tv_end.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.spinner_name:
//                ARouter.getInstance().build(Page.ACTIVITY_PUNISH_SHOP).navigation();
                Intent intent = new Intent(mContext, ShopListActivity.class);
                startActivityForResult(intent, 1100);
                break;
            case R.id.btn_update:
                updateData();
                break;
            case R.id.btn_del:
                deleteData();
                break;
            case R.id.tv_start:
                new DatePickerDialog(mContext, AlertDialog.THEME_TRADITIONAL, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Log.d(TAG,"i="+i+",i1="+i1+",i2="+i2);
                        tv_start.setText(i+"-"+(i1+1)+"-"+i2);
                        punishBean.setStartTime(i+"-"+(i1+1)+"-"+i2);
                    }
                }, mYear, mMonth, mDay).show();
                break;
            case R.id.tv_end:
                new DatePickerDialog(mContext, AlertDialog.THEME_TRADITIONAL, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Log.d(TAG,"i="+i+",i1="+i1+",i2="+i2);
                        tv_end.setText(i+"-"+(i1+1)+"-"+i2);
                        punishBean.setEndTime(i+"-"+(i1+1)+"-"+i2);
                    }
                }, mYear, mMonth, mDay).show();
                break;
        }
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1100 && resultCode == RESULT_OK) {
            shopBean = (ShopBean) data.getSerializableExtra("office");
            punishBean.setOfficeId(shopBean.getId());
            punishBean.setLegalPerson(shopBean.getLegalPerson());
            Log.i(TAG, shopBean.getName());
            tv_shopName.setText(shopBean.getName());
            tv_legalPerson.setText(shopBean.getLegalPerson());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG,"isSetData="+isSetData);
        switch (adapterView.getId()) {
            case R.id.spinner_mode:
                if (!isSetData) {
                    Log.d(TAG,"i="+i+",l="+l);
                    punishBean.setPunishWay(String.valueOf(i+1));
                }
                break;
            case R.id.spinner_breakMode://违法类型
                if (!isSetData) {
                    Log.d(TAG,"spinner_breakMode  i="+i+",l="+l);
//                        getReasonData("2",punishBean.getReasonId());
                    if (i != 0) {
                        ReasonBean reasonBean = reasonBeans.get(i-1);
                        punishBean.setReasonId(reasonBean.getId());
                        punishBean.setReason(reasonBean.getName());
                        Log.d(TAG,"违法类型  ="+reasonBean.getName());
                        getReasonData("2",reasonBean.getId());
                        et_reason.setText(reasonBean.getName());
                        et_evidence.setText("");
                        et_lawType.setText("");
                        et_lawFix.setText("");
                        et_lawAmend.setText("");
                    } else {
                        evidenceArr = new String[]{""};
                        punishBean.setReasonId("");
                        punishBean.setReason("");
                        ArrayAdapter<String> breakAdapter = new ArrayAdapter<String>(mContext,R.layout.punish_spinner_item,evidenceArr);
                        breakAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_evidence.setAdapter(breakAdapter);
                    }
                }
                break;
            case R.id.spinner_breakAction:
                if (!isSetData) {
                    if (i != 0) {
                        ReasonBean reasonBean = evidenceBeans.get(i-1);
                        punishBean.setEvidenceId(reasonBean.getId());
                        punishBean.setEvidence(reasonBean.getName());
                        Log.d(TAG,"spinner_breakAction  i="+i+",l="+l+","+reasonBean.getName());
                        et_evidence.setText(reasonBean.getName());
                        //当违法行为选择的数据变化时，清空下面的数据
                        et_lawType.setText("");
                        et_lawFix.setText("");
                        et_lawAmend.setText("");
                        //获取违法证据
                        getReasonData("5",reasonBean.getId());
                        //获取违法规定
                        getReasonData("3",reasonBean.getId());
                        //获取违法改正
                        getReasonData("4",reasonBean.getId());
                    } else {
                        lawTypeArr = new String[]{""};
                        punishBean.setEvidenceId("");
                        punishBean.setEvidence("");
                        ArrayAdapter<String> breakAdapter = new ArrayAdapter<String>(mContext,R.layout.punish_spinner_item,lawTypeArr);
                        breakAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_lawType.setAdapter(breakAdapter);
                        lawFixArr = new String[]{""};
                        ArrayAdapter<String> fixAdapter = new ArrayAdapter<String>(mContext,R.layout.punish_spinner_item,lawFixArr);
                        breakAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_lawFix.setAdapter(fixAdapter);
                        lawAmendArr = new String[]{""};
                        ArrayAdapter<String> amendAdapter = new ArrayAdapter<String>(mContext,R.layout.punish_spinner_item,lawAmendArr);
                        breakAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_lawAmend.setAdapter(amendAdapter);
                    }
                }
                break;
            case R.id.spinner_breakEvidence://违法证据
                if (i != 0) {
                    ReasonBean reasonBean = lawTypeBeans.get(i-1);
                    punishBean.setLawTypeId(reasonBean.getId());
                    punishBean.setLawType(reasonBean.getName());
                    et_lawType.setText(reasonBean.getName());
                    Log.d(TAG,"违法证据  i="+i+",l="+l+","+reasonBean.getName());
                } else {
                    punishBean.setLawTypeId("");
                    punishBean.setLawType("");
                }
                break;
            case R.id.spinner_breakSet://违法规定
                if (i != 0) {
                    ReasonBean reasonBean = lawFixBeans.get(i-1);
                    punishBean.setLawFixId(reasonBean.getId());
                    punishBean.setLawFix(reasonBean.getName());
                    et_lawFix.setText(reasonBean.getName());
                    Log.d(TAG,"违法规定  i="+i+",l="+l+","+reasonBean.getName());
                } else {
                    punishBean.setLawFixId("");
                    punishBean.setLawFix("");
                }
                break;
            case R.id.spinner_breakCorrect://违法改正
                if (i != 0) {
                    ReasonBean reasonBean = lawAmendBeans.get(i-1);
                    punishBean.setLawAmendId(reasonBean.getId());
                    punishBean.setLawAmend(reasonBean.getName());
                    et_lawAmend.setText(reasonBean.getName());
                    Log.d(TAG,"违法改正  i="+i+",l="+l+","+reasonBean.getName());
                } else {
                    punishBean.setLawAmendId("");
                    punishBean.setLawAmend("");
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
    
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            isSetData = false;
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return true;
    }
    
    private void updateData() {
        if (StringUtils.isEmpty(tv_punishNo.getText().toString())) {
            Toast.makeText(mContext,"请填写处罚单编号！",Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtils.isEmpty(tv_punishTitle.getText().toString())) {
            Toast.makeText(mContext,"请填写处罚单标题！",Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, Object> baseParam = WebFrontUtil.getBaseParam();
        baseParam.put("isNewRecord", false);
        baseParam.put("id", punishBean.getId());
        baseParam.put("officeId",punishBean.getOfficeId());
        baseParam.put("legalPerson",punishBean.getLegalPerson());
        String punishNo = tv_punishNo.getText().toString();
        punishBean.setPunishNo(punishNo);
        baseParam.put("punishNo",punishBean.getPunishNo());
        baseParam.put("punishWay",punishBean.getPunishWay());
        String agency = tv_reviewAgency.getText().toString();
        punishBean.setReviewAgency(agency);
        baseParam.put("reviewAgency",punishBean.getReviewAgency());
        baseParam.put("dueTimeFrom",startTime);
        baseParam.put("dueTimeTo",endTime);
        baseParam.put("reasonId",punishBean.getReasonId());
        String reason = et_reason.getText().toString();
        baseParam.put("reason",reason);
        baseParam.put("evidenceId",punishBean.getEvidenceId());
        String evidence = et_evidence.getText().toString();
        baseParam.put("evidence",evidence);
        baseParam.put("lawTypeId",punishBean.getLawTypeId());
        String lawType = et_lawType.getText().toString();
        baseParam.put("lawType",lawType);
        baseParam.put("lawFixId",punishBean.getLawFixId());
        String lawFix = et_lawFix.getText().toString();
        baseParam.put("lawFix",lawFix);
        baseParam.put("lawAmendId",punishBean.getLawAmendId());
        String lawAmend = et_lawAmend.getText().toString();
        baseParam.put("lawAmend",lawAmend);
        String layItem = tv_lawItem.getText().toString();
        punishBean.setLawItem(layItem);
        baseParam.put("lawItem",punishBean.getLawItem());
        String remarks = tv_breakDesc.getText().toString();
        punishBean.setRemarks(remarks);
        baseParam.put("remarks",punishBean.getRemarks());
        baseParam.put("punishTitle",tv_punishTitle.getText().toString());
        baseParam.put("punishTel",tv_punishTel.getText().toString());
        if (!StringUtils.isEmpty(punishBean.getApplyType())) {
            baseParam.put("applyType",punishBean.getApplyType());
        }

        OkHttpUtil.post(TAG, WebApi.PUNISH_UPDATE_ADD_URL, baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.d(TAG,""+e);
            }
            
            @Override
            public void onResponse(String response, int id) {
                try {
                    JSONObject object = new JSONObject(response);
                    if ((int)object.get("code") == 200) {
                        finish();
                    } else {
                        ToastUtils.showMsg(object.get("message").toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void deleteData() {
        HashMap<String, Object> baseParam = WebFrontUtil.getBaseParam();
        OkHttpUtil.post(TAG, WebApi.PUNISH_DEL_URL+"?id="+punishBean.getId(), baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.d(TAG,"deleteData    "+e);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d(TAG,"response==="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    if ((int)object.get("code") == 200) {
                        finish();
                    } else {
                        ToastUtils.showMsg(object.get("message").toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
