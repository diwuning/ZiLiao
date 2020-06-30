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
}
