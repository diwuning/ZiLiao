package com.nmpa.nmpaapp.modules.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.base.BaseActivity;
import com.nmpa.nmpaapp.base.ui.StatusBarUtil;
import com.nmpa.nmpaapp.constants.Const;
import com.nmpa.nmpaapp.constants.WebApi;
import com.nmpa.nmpaapp.http.BaseResponseObject;
import com.nmpa.nmpaapp.http.OkHttpUtil;
import com.nmpa.nmpaapp.http.WebFrontUtil;
import com.nmpa.nmpaapp.modules.huanxin.utils.DemoHelper;
import com.nmpa.nmpaapp.router.Page;
import com.nmpa.nmpaapp.utils.SavePreferences;
import com.nmpa.nmpaapp.utils.StringUtils;
import com.nmpa.nmpaapp.utils.ToastUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import java.util.HashMap;
import java.util.Map;
import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
@Route(path = Page.ACTIVITY_LOGIN)
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    @BindView(R.id.et_login_name)
    EditText mEditTextName;
    @BindView(R.id.et_login_password)
    EditText mEditTextPassword;
    @BindView(R.id.btn_login)
    Button mButtonLogin;
    private String name;
    private String password;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBeforeSetContentView() {
        StatusBarUtil.translucent(this);
        StatusBarUtil.setStatusBarLightMode(this);
    }
    
    @Override
    public int getLayoutResID() {
        return R.layout.activity_login;
    }

    @Override
    public void initContentView(@Nullable Bundle savedInstanceState) {
        mEditTextName.addTextChangedListener(mTextWatcherName);
        mEditTextPassword.addTextChangedListener(mTextWatcherPassword);
    }
    
    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @OnClick(R.id.btn_login)
    public void loginOnClic() {
        name = mEditTextName.getText().toString();
        password = mEditTextPassword.getText().toString();

        if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(password)) {
            HashMap<String, Object> baseParam = WebFrontUtil.getBaseParam();
            baseParam.put("loginName",name);
            baseParam.put("password",password);
            OkHttpUtil.post(TAG, WebApi.app_login, baseParam, new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {

                }
                
                @Override
                public void onResponse(String response, int id) {
                    Log.d(TAG,"loginOnClic response="+response);
                    BaseResponseObject responseObject = WebFrontUtil.getResponseObject(response);
                    String responseCode = responseObject.getResponseCode();
                    if (!StringUtils.isEmpty(responseCode) && "200".equals(responseCode)) {
                        Map responseData = responseObject.getResponseData();
                        String token = StringUtils.getMapString(responseData, "token");
                        SavePreferences.setData(Const.TOKEN_KEY, token);
                        String userId = StringUtils.getMapString(responseData,"id");
                        SavePreferences.setData(Const.USER_ID,userId);
                        String userName = StringUtils.getMapString(responseData,"name");
                        SavePreferences.setData(Const.USER_NAME,userName);
                        ToastUtils.showMsg("登陆成功");
                        login();
                        toMain();
                    } else {
                        ToastUtils.showMsg(responseObject.getResponseMessage());
                    }
                }
            });
        }
    }
    
    private void login() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                EMClient.getInstance().login(name,
                        password, new EMCallBack() {
                            @Override
                            public void onSuccess() {
                                EMClient.getInstance().groupManager().loadAllGroups();
                                EMClient.getInstance().chatManager().loadAllConversations();
                                Log.d(TAG,"登录聊天服务器成功");
//                                DemoDBManager.getInstance().closeDB();
                                DemoHelper.getInstance().setCurrentUserName(name);
//                                DemoHelper.getInstance().getModel().setMsgRoaming(true);
                                //加上这一句，退出应用再进，就不用重新登录了
                                DemoHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this,"登录聊天服务器成功",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(int code, String error) {
                                Log.d(TAG,"登录聊天服务器失败");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this,"登录聊天服务器失败",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            
                            @Override
                            public void onProgress(int progress, String status) {

                            }
                        });
            }
        }).start();
    }
    
    private void toMain() {
        ARouter.getInstance().build(Page.ACTIVITY_MAIN).navigation();
        finish();
    }

    TextWatcher mTextWatcherName = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                if (!StringUtils.isEmpty(password)) {
                    mButtonLogin.setEnabled(true);
                    mButtonLogin.setTextColor(
                            ContextCompat.getColor(LoginActivity.this, R.color.color_ffffff));
                }
            }
        }
        
        @Override
        public void afterTextChanged(Editable s) {
            name = mEditTextName.getText().toString();
            if (s.length() == 0) {
                mButtonLogin.setEnabled(false);
                mButtonLogin.setTextColor(
                        ContextCompat.getColor(LoginActivity.this, R.color.color_8f8f8f));
            }
        }
    };
    
    TextWatcher mTextWatcherPassword = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                if (!StringUtils.isEmpty(name)) {
                    mButtonLogin.setEnabled(true);
                    mButtonLogin.setTextColor(
                            ContextCompat.getColor(LoginActivity.this, R.color.color_ffffff));
                }
            }
        }
        
        @Override
        public void afterTextChanged(Editable s) {
            password = mEditTextPassword.getText().toString();
            if (s.length() == 0) {
                mButtonLogin.setEnabled(false);
                mButtonLogin.setTextColor(
                        ContextCompat.getColor(LoginActivity.this, R.color.color_8f8f8f));
            }
        }
    };
}
