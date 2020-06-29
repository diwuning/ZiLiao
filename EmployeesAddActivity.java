package com.nmpa.nmpaapp.modules.employees;

import androidx.annotation.Nullable;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.gson.Gson;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.base.BaseActivity;
import com.nmpa.nmpaapp.base.ui.AppBarConfig;
import com.nmpa.nmpaapp.constants.WebApi;
import com.nmpa.nmpaapp.http.OkHttpUtil;
import com.nmpa.nmpaapp.http.WebFrontUtil;
import com.nmpa.nmpaapp.modules.employees.bean.EmployeeBean;
import com.nmpa.nmpaapp.modules.employees.bean.WorkType;
import com.nmpa.nmpaapp.modules.office.adapter.ImgGridAdapter;
import com.nmpa.nmpaapp.router.Page;
import com.nmpa.nmpaapp.utils.FileLoadUtils;
import com.nmpa.nmpaapp.utils.SavePreferences;
import com.nmpa.nmpaapp.utils.StringUtils;
import com.nmpa.nmpaapp.widget.CustomSpinner;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
@Route(path = Page.ACTIVITY_EMPLOYEES_ADD)
public class EmployeesAddActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "EmployeesAddActivity";
    private Context mContext;
    @BindView(R.id.tv_name)
    EditText tv_name;
    @BindView(R.id.spinner_workType)
    Spinner spinner_workType;
    @BindView(R.id.rg_sex)
    RadioGroup rg_sex;
    @BindView(R.id.rb_male)
    RadioButton rb_male;
    @BindView(R.id.rb_female)
    RadioButton rb_female;
    @BindView(R.id.tv_age)
    TextView tv_age;
    @BindView(R.id.spinner_folk)
    Spinner spinner_folk;
 
    @BindView(R.id.tv_politicalStatus)
    TextView tv_politicalStatus;
    @BindView(R.id.tv_tel)
    EditText tv_tel;
    @BindView(R.id.tv_webchat)
    EditText tv_webchat;
    @BindView(R.id.tv_nativeAddr)
    EditText tv_nativeAddr;
    @BindView(R.id.tv_address)
    EditText tv_address;
 
    @BindView(R.id.tv_valid)
    TextView tv_valid;
    @BindView(R.id.tv_issueDate)
    TextView tv_issueDate;
    @BindView(R.id.tv_employee_remarks)
    EditText tv_employee_remarks;
    @BindView(R.id.tv_upload)
    TextView tv_upload;
 
    @BindView(R.id.rv_health)
    GridView rv_health;
 
    @BindView(R.id.tv_idCard)
    EditText tv_idCard;
    @BindView(R.id.tv_birthDay)
    TextView tv_birthDay;
    @BindView(R.id.tv_issuingAuthority)
    EditText tv_issuingAuthority;
    @BindView(R.id.tv_healthNum)
    EditText tv_healthNum;
    @BindView(R.id.tv_empPost)
    EditText tv_empPost;
    @BindView(R.id.spinner_techTitle)
    Spinner spinner_techTitle;
    @BindView(R.id.tv_uploadPhoto)
    TextView tv_uploadPhoto;
    @BindView(R.id.gv_photo)
    GridView gv_photo;
    @BindView(R.id.tv_uploadRcolp)
    TextView tv_uploadRcolp;
    @BindView(R.id.gv_rcolp)
    GridView gv_rcolp;
    @BindView(R.id.tv_uploadDrugLic)
    TextView tv_uploadDrugLic;
    @BindView(R.id.rv_drugLic)
    GridView rv_drugLic;
    @BindView(R.id.ll_medical)
    LinearLayout ll_medical;
 
    private int mYear, mMonth, mDay;
//    private List<ImageView> imageViews = new ArrayList<>();
    String[] folkArr;
    private List<WorkType> folks = new ArrayList<>();
    String[] workTypeArr;
    private List<WorkType> workTypes = new ArrayList<>();
    private List<WorkType> techTtiles = new ArrayList<>();
    private String officeId = "";
    private String status = "";
    private EmployeeBean employeeBean;
    private String cateringType = "";
 
    @Override
    public void onBeforeSetContentView() {
 
    }
 
    @Override
    public int getLayoutResID() {
        return R.layout.activity_employees_add;
    }
 
    @Override
    protected CharSequence setActionBarTitle() {
        return "添加从业人员";
    }
 
    @Nullable
    @Override
    public AppBarConfig getAppBarConfig() {
        return mAppBarCompat;
    }
 
    @Override
    public int setActionBarRightVisibility() {
        return View.VISIBLE;
    }
 
    @Override
    public CharSequence setActionBarRightText() {
        return "提交";
    }
 
    @Override
    protected void actionBarRightOnClick() {
        addEmployee();
    }
 
    @Override
    public void initContentView(@Nullable Bundle savedInstanceState) {
        mContext = EmployeesAddActivity.this;
        tv_valid.setOnClickListener(this);
        tv_issueDate.setOnClickListener(this);
        tv_birthDay.setOnClickListener(this);
        tv_upload.setOnClickListener(this);
        Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
        Log.d(TAG,"mYear="+mYear+",mMonth="+mMonth+",mDay="+mDay);
        tv_uploadPhoto.setOnClickListener(this);
        tv_uploadRcolp.setOnClickListener(this);
        tv_uploadDrugLic.setOnClickListener(this);
        rb_male.setChecked(true);
        requestCameraPermission();
 
    }
 
    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (getIntent().getStringExtra("officeId") != null) {
            officeId = getIntent().getStringExtra("officeId");
        }
 
        if (getIntent().getStringExtra("from") != null) {
            status = getIntent().getStringExtra("from");
        }
 
        if (getIntent().getStringExtra("CateringType") != null) {
            cateringType = getIntent().getStringExtra("CateringType");
            Log.e(TAG,"cateringType=="+cateringType);
            if (cateringType.contains("药品") || cateringType.contains("医疗")) {
                ll_medical.setVisibility(View.VISIBLE);
            } else {
                ll_medical.setVisibility(View.GONE);
            }
        }
 
        getFolkData();
        getWorkTypeData();
        getTechTitleData();
 
        if (status.equals("update")) {
            if (getIntent().getSerializableExtra("employeebean") != null) {
                employeeBean = (EmployeeBean) getIntent().getSerializableExtra("employeebean");
                setEmployeeData();
                officeId = employeeBean.getOfficeId();
            }
        }
    }
 
    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23 ) {
//            isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {
                    Manifest.permission.CAMERA//,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
 
            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入到这里代表没有权限.
                }
            }
 
            if (!permissionsList.isEmpty()) {
                Log.e(TAG,"permissionsList.isEmpty()="+permissionsList.isEmpty());
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 0);
            } else {
 
            }
        }
    }
 
    String delEPath = "";
    String delPhotoPath = "";
    String delRecolpPath = "";
    String delDrugPath = "";
    private void setEmployeeData() {
        tv_name.setText(employeeBean.getName());
        String sex = employeeBean.getSex();
        if (!sex.equals("") && sex.equals("男")) {
            rb_male.setChecked(true);
        } else {
            rb_female.setChecked(true);
        }
        tv_age.setText(String.valueOf(employeeBean.getAge()));
        tv_politicalStatus.setText(employeeBean.getPoliticalStatus());
        tv_tel.setText(employeeBean.getTel());
        tv_webchat.setText(employeeBean.getWebchat());
        tv_nativeAddr.setText(employeeBean.getNativeAddr());
        tv_address.setText(employeeBean.getAddress());
        String validStr = employeeBean.getValidDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (validStr != null) {
            Date validDate = new Date(Long.valueOf(validStr));
            tv_valid.setText(dateFormat.format(validDate));
        }
 
        String issueDateStr = employeeBean.getIssueDate();
        if (issueDateStr != null) {
            Date issueDate = new Date(Long.valueOf(issueDateStr));
            tv_issueDate.setText(dateFormat.format(issueDate));
        }
        tv_employee_remarks.setText(employeeBean.getRemarks());
        setHealthData();
 
        tv_birthDay.setText(employeeBean.getBirthDay());
        tv_issuingAuthority.setText(employeeBean.getIssuingAuthority());
        tv_healthNum.setText(employeeBean.getHealthNum());
        tv_idCard.setText(employeeBean.getIdCard());
 
        setPhotoData();
        tv_empPost.setText(employeeBean.getEmpPost());
        setRcolpData();
        setDrugData();
    }
 
    private void setHealthData() {
        String healthStr = employeeBean.getPic();
 
        if (healthStr != null && !healthStr.equals("")) {
            String[] healthArr = healthStr.split(",");
            pathList.add(healthArr[0]);
            if (healthArr.length >1) {
                pathList.add(healthArr[1]);
            }
            ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext,pathList);
            rv_health.setAdapter(imageAdapter);
            count = pathList.size()-1;
            imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
                @Override
                public void onItemDelClick(int position) {
                    count = count - 1;
                    Log.e(TAG,"delPath="+pathList.get(position));
                    if (pathList.get(position).startsWith("http")) {
                        delEPath = delEPath+pathList.get(position)+",";
                    }
                    pathList.remove(position);
                    imageAdapter.notifyDataSetChanged();
                }
            });
        }
    }
 
    private void setPhotoData() {
        String photo = employeeBean.getPhotoUrl();
        if (photo != null && !photo.equals("")) {
            String[] healthArr = photo.split(",");
            photoPathList.add(healthArr[0]);
            if (healthArr.length >1) {
                photoPathList.add(healthArr[1]);
            }
 
            ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext,photoPathList);
            gv_photo.setAdapter(imageAdapter);
            photoCount = photoPathList.size()-1;
            imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
                @Override
                public void onItemDelClick(int position) {
                    photoCount = photoCount - 1;
                    Log.e(TAG,"delPhotoPath="+photoPathList.get(position));
                    if (photoPathList.get(position).startsWith("http")) {
                        delPhotoPath = delPhotoPath + photoPathList.get(position)+",";
                    }
                    photoPathList.remove(position);
                    imageAdapter.notifyDataSetChanged();
                }
            });
        }
    }
 
    private void setRcolpData() {
        String rcolpUrl = employeeBean.getRcolpUrl();
        if (rcolpUrl != null && !rcolpUrl.equals("")) {
            String[] healthArr = rcolpUrl.split(",");
            recolpPathList.add(healthArr[0]);
            if (healthArr.length >1) {
                recolpPathList.add(healthArr[1]);
            }
            ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext,recolpPathList);
            gv_rcolp.setAdapter(imageAdapter);
            recolpCount = recolpPathList.size()-1;
            imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
                @Override
                public void onItemDelClick(int position) {
                    recolpCount = recolpCount - 1;
                    Log.e(TAG,"delRecolpPath="+recolpPathList.get(position));
                    if (recolpPathList.get(position).startsWith("http")) {
                        delRecolpPath = delRecolpPath + recolpPathList.get(position)+",";
                    }
                    recolpPathList.remove(position);
                    imageAdapter.notifyDataSetChanged();
                }
            });
        }
    }
    
    private void setDrugData() {
        String druglicUrl = employeeBean.getDruglicUrl();
        if (druglicUrl != null && !druglicUrl.equals("")) {
            String[] healthArr = druglicUrl.split(",");
            drugPathList.add(healthArr[0]);
            if (healthArr.length >1) {
                drugPathList.add(healthArr[1]);
            }
            ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext,drugPathList);
            rv_drugLic.setAdapter(imageAdapter);
            drugCount = drugPathList.size()-1;
            imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
                @Override
                public void onItemDelClick(int position) {
                    drugCount = drugCount - 1;
                    Log.e(TAG,"delDrugPath="+drugPathList.get(position));
                    if (drugPathList.get(position).startsWith("http")) {
                        delDrugPath = delDrugPath + drugPathList.get(position)+",";
                    }
                    drugPathList.remove(position);
                    imageAdapter.notifyDataSetChanged();
                }
            });
        }
    }
 
    /*
    * 获取民族列表
    *
    * */
    private void getFolkData() {
        HashMap<String, Object> baseParam = WebFrontUtil.getBaseParam();
        baseParam.put("type","folk");
        OkHttpUtil.post(TAG, WebApi.PUNISH_WAY_URL, baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG,"getFolkData  e="+e);
            }
 
            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG,"getFolkData  response="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    if ((int)object.get("code") == 200) {
                        JSONArray array = object.getJSONArray("data");
                        folkArr = new String[array.length()];
                        Gson gson = new Gson();
                        for (int i=0;i<array.length();i++) {
                            WorkType workType = gson.fromJson(array.get(i).toString(), WorkType.class);
                            folks.add(workType);
                            folkArr[i] = workType.getLabel();
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter(mContext,R.layout.punish_spinner_item,folkArr);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_folk.setAdapter(adapter);
                        if (status.equals("update")) {
                            int forkIndex = 0;
                            for (int i=0;i<folkArr.length;i++) {
                                if (folkArr[i].equals(employeeBean.getFolk())) {
                                    forkIndex = i;
                                }
                            }
                            spinner_folk.setSelection(forkIndex);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    //专业技术职称列表
    String[] techTitleArr;
    private void getTechTitleData() {
        HashMap<String, Object> baseParam = WebFrontUtil.getBaseParam();
        baseParam.put("type","tech_title");
        OkHttpUtil.post(TAG, WebApi.PUNISH_WAY_URL, baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG,"getTechTitleData  e="+e);
            }
            
            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG,"getTechTitleData  response="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    if ((int)object.get("code") == 200) {
                        JSONArray array = object.getJSONArray("data");
                        techTitleArr = new String[array.length()];
                        Gson gson = new Gson();
                        for (int i=0;i<array.length();i++) {
                            WorkType workType = gson.fromJson(array.get(i).toString(), WorkType.class);
                            techTtiles.add(workType);
                            techTitleArr[i] = workType.getLabel();
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter(mContext,R.layout.punish_spinner_item,techTitleArr);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_techTitle.setAdapter(adapter);
                        if (status.equals("update")) {
                            int index = 0;
                            for (int i=0;i<techTitleArr.length;i++) {
                              if (!StringUtils.isEmpty(employeeBean.getTechTitle()) && Integer.valueOf(employeeBean.getTechTitle()) == i) {
                                    index = i;
                                }
                            }
                            spinner_techTitle.setSelection(index);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    
    private void getWorkTypeData() {
        HashMap<String, Object> baseParam = WebFrontUtil.getBaseParam();
        baseParam.put("type","work_type");
        OkHttpUtil.post(TAG, WebApi.PUNISH_WAY_URL, baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG,"getWorkTypeData  e="+e);
            }
            
            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG,"getWorkTypeData  response="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    if ((int)object.get("code") == 200) {
                        JSONArray array = object.getJSONArray("data");
                        workTypeArr = new String[array.length()];
                        Gson gson = new Gson();
                        for (int i=0;i<array.length();i++) {
                            WorkType workType = gson.fromJson(array.get(i).toString(), WorkType.class);
                            workTypes.add(workType);
                            workTypeArr[i] = workType.getLabel();
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter(mContext,R.layout.punish_spinner_item,workTypeArr);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_workType.setAdapter(adapter);
                        if (status.equals("update")) {
                            int index = 0;
                            for (int i=0;i<workTypeArr.length;i++) {
                                if (workTypeArr[i].equals(employeeBean.getWorkType())) {
                                    index = i;
                                }
                            }
                            spinner_workType.setSelection(index);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
 
    /*
    * 把所有数据保存到EmployeeBean实体类中
    * */
    private EmployeeBean setEmployeeBean() {
        EmployeeBean employeeBeanNew = new EmployeeBean();
        if (employeeBean != null) {
            employeeBeanNew.setId(employeeBean.getId());
        }
        employeeBeanNew.setName(tv_name.getText().toString());
 
        if (rb_female.isChecked()) {
            employeeBeanNew.setSex("2");
        } else if(rb_male.isChecked()){
            employeeBeanNew.setSex("1");
        }
        String age = tv_age.getText().toString();
        if (age != null && !age.equals("")) {
            employeeBeanNew.setAge(Integer.valueOf(age));
        }
        employeeBeanNew.setTel(tv_tel.getText().toString());
        employeeBeanNew.setNativeAddr(tv_nativeAddr.getText().toString());
        employeeBeanNew.setAddress(tv_address.getText().toString());
        employeeBeanNew.setFolk(String.valueOf(spinner_folk.getSelectedItemPosition()));
        employeeBeanNew.setWorkType(String.valueOf(spinner_workType.getSelectedItemPosition()));
        employeeBeanNew.setValidDate(tv_valid.getText().toString());
        employeeBeanNew.setIdCard(tv_idCard.getText().toString());
        employeeBeanNew.setBirthDay(tv_birthDay.getText().toString());
        employeeBeanNew.setHealthNum(tv_healthNum.getText().toString());
        employeeBeanNew.setIssueDate(tv_issueDate.getText().toString());
        employeeBeanNew.setIssuingAuthority(tv_issuingAuthority.getText().toString());
        employeeBeanNew.setRemarks(tv_employee_remarks.getText().toString());
        employeeBeanNew.setWebchat(tv_webchat.getText().toString());
 
        employeeBeanNew.setEmpPost(tv_empPost.getText().toString());
        employeeBeanNew.setTechTitle(String.valueOf(spinner_techTitle.getSelectedItemPosition()));
        return employeeBeanNew;
    }
 
    private void addEmployee() {
        EmployeeBean employeeBean = setEmployeeBean();
        String path="",path1="";
        String pic = "";
        File file = null;
        File file1 = null;
        if (pathList != null && pathList.size() != 0) {
            path = pathList.get(0);
            if (path.startsWith("http")) {
                pic = path;
            } else {
                file = new File(path);
            }
            if (pathList.size()>1) {
                path1 = pathList.get(1);
                if (path1.startsWith("http")) {
                    if (pic.equals("")) {
                        pic = path1;
                    } else {
                        pic = pic+","+path1;
                    }
                } else {
                    file1 = new File(path1);
                }
            }
        }
        employeeBean.setPic(pic);
 
        File file2 = null;
        File file3 = null;
        File file4 = null;
        File file5 = null;
        if (cateringType.contains("药品") || cateringType.contains("医疗")) {
            String rcolpUrl = "";
            if (recolpPathList != null && recolpPathList.size() != 0) {
                String path2 = recolpPathList.get(0);
                if (path2.startsWith("http")) {
                    rcolpUrl = path2;
                } else {
                    file2 = new File(path2);
                }
                if (recolpPathList.size()>1) {
                    String path3 = recolpPathList.get(1);
                    if (path3.startsWith("http")) {
                        if (rcolpUrl.equals("")) {
                            rcolpUrl = path1;
                        } else {
                            rcolpUrl = rcolpUrl+","+path3;
                        }
                    } else {
                        file3 = new File(path3);
                    }
                }
            }
            employeeBean.setRcolpUrl(rcolpUrl);
 
            String drugUrl = "";
            if (drugPathList != null && drugPathList.size() != 0) {
                String path2 = drugPathList.get(0);
                if (path2.startsWith("http")) {
                    drugUrl = path2;
                } else {
                    file4 = new File(path2);
                }
                if (drugPathList.size()>1) {
                    String path3 = drugPathList.get(1);
                    if (path3.startsWith("http")) {
                        if (drugUrl.equals("")) {
                            drugUrl = path1;
                        } else {
                            drugUrl = drugUrl+","+path3;
                        }
                    } else {
                        file5 = new File(path3);
                    }
                }
            }
            employeeBean.setDruglicUrl(drugUrl);
        }
 
        File file6 = null;
        String photoUrl = "";
        if (photoPathList != null && photoPathList.size() != 0) {
            String path2 = photoPathList.get(0);
            if (path2.startsWith("http")) {
                photoUrl = path2;
            } else {
                file6 = new File(path2);
            }
        }
 
        employeeBean.setPhotoUrl(photoUrl);
//                FileLoadUtils.uploadFile(employeeBean);
        Map<String, String> baseParam = new HashMap<>();
        if (status.equals("update")) {
            baseParam.put("id",employeeBean.getId());
        }
 
        baseParam.put("name",employeeBean.getName());
        baseParam.put("office.id",officeId);
        baseParam.put("gender",employeeBean.getSex());
        baseParam.put("age",String.valueOf(employeeBean.getAge()));
        baseParam.put("tel",employeeBean.getTel());
        baseParam.put("idCard",employeeBean.getIdCard());
        baseParam.put("birthDay",employeeBean.getBirthDay());
        baseParam.put("webchat",employeeBean.getWebchat());
        baseParam.put("folk",employeeBean.getFolk());
        baseParam.put("workType",employeeBean.getWorkType());
        baseParam.put("nativeAddr",employeeBean.getNativeAddr());
        baseParam.put("address",employeeBean.getAddress());
        baseParam.put("IssuingAuthority",employeeBean.getIssuingAuthority());
        baseParam.put("issueDate",employeeBean.getIssueDate());
        baseParam.put("healthNum",employeeBean.getHealthNum());
        baseParam.put("validDate",employeeBean.getValidDate());
        baseParam.put("remarks",employeeBean.getRemarks());
        baseParam.put("empPost",employeeBean.getEmpPost());
        baseParam.put("techTitle",employeeBean.getTechTitle());
        if (status.equals("update")) {
            baseParam.put("picUrl",employeeBean.getPic());
            baseParam.put("picDelUrls",delEPath);
            baseParam.put("photoUrl",employeeBean.getPhotoUrl());
            baseParam.put("photoDelUrls",delPhotoPath);
            if (cateringType.contains("药品") || cateringType.contains("医疗")) {
                baseParam.put("rcolpUrl",employeeBean.getRcolpUrl());
                baseParam.put("rcolpDelUrls",delRecolpPath);
                baseParam.put("druglicUrl",employeeBean.getDruglicUrl());
                baseParam.put("druglicDelUrls",delDrugPath);
            }
 
        }
 
        FileLoadUtils.postEmpFile(WebApi.PUNISH_EMPLOYEE_ADD, baseParam,"picArray",file,file1,
                "rcolpArray",file2,file3, "druglicArray",file4,file5,"photoArray",file6, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"addEmployee  e="+e);
            }
 
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = FileLoadUtils.getResponseBody(response);
                Log.e(TAG,"addEmployee  result="+result);
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext,"添加成功",Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                    response.close();
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,"添加失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
 
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_valid:
                new DatePickerDialog(mContext, AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        tv_valid.setText(dataFormat(i,i1,i2));
                    }
                }, mYear, mMonth, mDay).show();
                break;
            case R.id.tv_issueDate:{
                new DatePickerDialog(mContext, AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        tv_issueDate.setText(dataFormat(i,i1,i2));
                    }
                }, mYear, mMonth, mDay).show();
                break;
            }
            case R.id.tv_birthDay: {
                new DatePickerDialog(mContext, AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        tv_birthDay.setText(dataFormat(i,i1,i2));
                    }
                }, mYear, mMonth, mDay).show();
                break;
            }
            case R.id.tv_upload:
                pickFile(2001);
                break;
            case R.id.tv_uploadPhoto:
                pickFile(2003);
                break;
            case R.id.tv_uploadRcolp:
                pickFile(2005);
                break;
            case R.id.tv_uploadDrugLic:
                pickFile(2007);
                break;
        }
    }
 
    // 打开系统的文件选择器
    public void pickFile(int requestCode) {
        FileLoadUtils.showPhotoDialog((Activity) mContext,requestCode);
    }
 
    int count = 0;
    int photoCount = 0;
    int recolpCount = 0;
    int drugCount = 0;
    private List<String> pathList = new ArrayList<>();
    private List<String> photoPathList = new ArrayList<>();
    private List<String> recolpPathList = new ArrayList<>();
    private List<String> drugPathList = new ArrayList<>();
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2001 && resultCode == RESULT_OK) {
            Log.e(TAG,"data="+data.getData()+","+data.getDataString());
            getHealthLocalPathData(data);
        } else if (requestCode == 2002 && resultCode == RESULT_OK) {
            Log.e(TAG,"data="+data.getData()+","+data.getDataString());
            getHealthTakePhotoData(data);
        } else if (requestCode == 2003 && resultCode == RESULT_OK) {
            Log.e(TAG,"data="+data.getData()+","+data.getDataString());
            getPhotoLocalPathData(data);
        }else if (requestCode == 2004 && resultCode == RESULT_OK) {
            Log.e(TAG,"data="+data.getData()+","+data.getDataString());
            getPhotoTakePhotoData(data);
        } else if (requestCode == 2005 && resultCode == RESULT_OK) {
            Log.e(TAG,"data="+data.getData()+","+data.getDataString());
            if (data == null) {
                return;
            }
            if (recolpCount == 2) {
                Toast.makeText(mContext,"最多上传2张照片",Toast.LENGTH_SHORT).show();
                return;
            }
            String str = FileLoadUtils.handleImageOnKitKat(mContext,data);
            Log.e(TAG,"str="+str);
            str = FileLoadUtils.saveBitmap(mContext,FileLoadUtils.getimage(str));
//            Glide.with(mContext).load(str).into(imageViews.get(count));
            recolpPathList.add(str);
            ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext, recolpPathList);
            gv_rcolp.setAdapter(imageAdapter);
            imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
                @Override
                public void onItemDelClick(int position) {
                    recolpCount = recolpCount - 1;
                    Log.e(TAG,"delRecolpPath="+recolpPathList.get(position));
                    if (recolpPathList.get(position).startsWith("http")) {
                        delRecolpPath = delRecolpPath + recolpPathList.get(position)+",";
                    }
                    recolpPathList.remove(position);
                    imageAdapter.notifyDataSetChanged();
                }
            });
            recolpCount ++;
        } else if (requestCode == 2006 && resultCode == RESULT_OK) {
            Log.e(TAG,"data="+data.getData()+","+data.getDataString());
            if (recolpCount == 2) {
                Toast.makeText(mContext,"最多上传2张照片",Toast.LENGTH_SHORT).show();
                return;
            }
            String str = FileLoadUtils.getCameraData(data);
            Log.e(TAG,"str="+str);
//            Glide.with(mContext).load(str).into(imageViews.get(count));
            recolpPathList.add(str);
            ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext, recolpPathList);
            gv_rcolp.setAdapter(imageAdapter);
            gv_rcolp.requestFocus();
            imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
                @Override
                public void onItemDelClick(int position) {
                    recolpCount = recolpCount - 1;
                    Log.e(TAG,"delRecolpPath="+recolpPathList.get(position));
                    if (recolpPathList.get(position).startsWith("http")) {
                        delRecolpPath = delRecolpPath + recolpPathList.get(position)+",";
                    }
                    recolpPathList.remove(position);
                    imageAdapter.notifyDataSetChanged();
                }
            });
            recolpCount ++;
        } else if (requestCode == 2007 && resultCode == RESULT_OK) {
            Log.e(TAG,"data="+data.getData()+","+data.getDataString());
            if (data == null) {
                return;
            }
            if (drugCount == 2) {
                Toast.makeText(mContext,"最多上传2张照片",Toast.LENGTH_SHORT).show();
                return;
            }
            String str = FileLoadUtils.handleImageOnKitKat(mContext,data);
            Log.e(TAG,"str="+str);
            str = FileLoadUtils.saveBitmap(mContext,FileLoadUtils.getimage(str));
            drugPathList.add(str);
            ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext, drugPathList);
            rv_drugLic.setAdapter(imageAdapter);
            imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
                @Override
                public void onItemDelClick(int position) {
                    drugCount = drugCount - 1;
                    Log.e(TAG,"delDrugPath="+recolpPathList.get(position));
                    if (drugPathList.get(position).startsWith("http")) {
                        delDrugPath = delDrugPath + drugPathList.get(position)+",";
                    }
                    drugPathList.remove(position);
                    imageAdapter.notifyDataSetChanged();
                }
            });
            drugCount ++;
        } else if (requestCode == 2008 && resultCode == RESULT_OK) {
          Log.e(TAG,"data="+data.getData()+","+data.getDataString());
            if (drugCount == 2) {
                Toast.makeText(mContext,"最多上传2张照片",Toast.LENGTH_SHORT).show();
                return;
            }
            String str = FileLoadUtils.getCameraData(data);
            Log.e(TAG,"str="+str);
            drugPathList.add(str);
            ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext, drugPathList);
            rv_drugLic.setAdapter(imageAdapter);
            rv_drugLic.requestFocus();
            imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
                @Override
                public void onItemDelClick(int position) {
                    drugCount = drugCount - 1;
                    Log.e(TAG,"delDrugPath="+recolpPathList.get(position));
                    if (drugPathList.get(position).startsWith("http")) {
                        delDrugPath = delDrugPath + drugPathList.get(position)+",";
                    }
                    drugPathList.remove(position);
                    imageAdapter.notifyDataSetChanged();
                }
            });
            drugCount ++;
        }
    }
    
    private void getHealthLocalPathData(Intent data) {
      if (data == null) {
            return;
        }
        if (count == 2) {
            Toast.makeText(mContext,"最多上传2张照片",Toast.LENGTH_SHORT).show();
            return;
        }
        String str = FileLoadUtils.handleImageOnKitKat(mContext,data);
        Log.e(TAG,"str="+str);
        str = FileLoadUtils.saveBitmap(mContext,FileLoadUtils.getimage(str));
        pathList.add(str);
        ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext, pathList);
        rv_health.setAdapter(imageAdapter);
        imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
            @Override
            public void onItemDelClick(int position) {
                count = count - 1;
                Log.e(TAG,"delPath="+pathList.get(position));
                if (pathList.get(position).startsWith("http")) {
                    delEPath = delEPath+pathList.get(position)+",";
                }
                pathList.remove(position);
                imageAdapter.notifyDataSetChanged();
            }
        });
        count ++;
    }
    
    private void getHealthTakePhotoData(Intent data) {
      if (count == 2) {
          Toast.makeText(mContext,"最多上传2张照片",Toast.LENGTH_SHORT).show();
          return;
      }
      String str = FileLoadUtils.getCameraData(data);
      Log.e(TAG,"str="+str);
      pathList.add(str);
      ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext, pathList);
      rv_health.setAdapter(imageAdapter);
      rv_health.requestFocus();
      imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
          @Override
          public void onItemDelClick(int position) {
              count = count - 1;
              Log.e(TAG,"delPath="+pathList.get(position));
              if (pathList.get(position).startsWith("http")) {
                  delEPath = delEPath+pathList.get(position)+",";
              }
              pathList.remove(position);
              imageAdapter.notifyDataSetChanged();
          }
      });
      count ++;
    }
    
    private void getPhotoLocalPathData(Intent data) {
        if (data == null) {
            return;
        }
        if (photoCount == 1) {
            Toast.makeText(mContext,"最多上传1张照片",Toast.LENGTH_SHORT).show();
            return;
        }
        String str = FileLoadUtils.handleImageOnKitKat(mContext,data);
        Log.e(TAG,"str="+str);
        str = FileLoadUtils.saveBitmap(mContext,FileLoadUtils.getimage(str));
//            Glide.with(mContext).load(str).into(imageViews.get(count));
        photoPathList.add(str);
        ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext, photoPathList);
        gv_photo.setAdapter(imageAdapter);
        imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
          @Override
            public void onItemDelClick(int position) {
                photoCount = photoCount - 1;
                Log.e(TAG,"delPath="+pathList.get(position));
                if (photoPathList.get(position).startsWith("http")) {
                    delPhotoPath = delPhotoPath+photoPathList.get(position)+",";
                }
                photoPathList.remove(position);
                imageAdapter.notifyDataSetChanged();
            }
        });
        photoCount ++;
    }

    private void getPhotoTakePhotoData(Intent data) {
        if (photoCount == 1) {
          Toast.makeText(mContext,"最多上传1张照片",Toast.LENGTH_SHORT).show();
            return;
        }
        String str = FileLoadUtils.getCameraData(data);
        Log.e(TAG,"str="+str);
//            Glide.with(mContext).load(str).into(imageViews.get(count));
        photoPathList.add(str);
        ImgGridAdapter imageAdapter = new ImgGridAdapter(mContext, photoPathList);
        gv_photo.setAdapter(imageAdapter);
        gv_photo.requestFocus();
        imageAdapter.setOnDelClickListener(new ImgGridAdapter.onDelClickListener() {
            @Override
            public void onItemDelClick(int position) {
                photoCount = photoCount - 1;
                Log.e(TAG,"delPath="+pathList.get(position));
                if (photoPathList.get(position).startsWith("http")) {
                    delPhotoPath = delPhotoPath+photoPathList.get(position)+",";
                }
                photoPathList.remove(position);
                imageAdapter.notifyDataSetChanged();
            }
        });
        photoCount ++;
    }
 
    private String dataFormat(int i,int i1,int i2) {
        String dataStr = "";
        if ((i1+1) <10) {
            if (i2 < 10) {
                dataStr=i+"-0"+(i1+1)+"-0"+i2;
            } else {
                dataStr = i+"-0"+(i1+1)+"-"+i2;
            }
        }else {
            if (i2 < 10) {
                dataStr = i+"-"+(i1+1)+"-0"+i2;
            } else {
                dataStr = i+"-"+(i1+1)+"-"+i2;
            }
        }
        return dataStr;
    }
}
