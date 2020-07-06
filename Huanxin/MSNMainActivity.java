package com.nmpa.nmpaapp.modules.huanxin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMClientListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMMultiDeviceListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.util.EMLog;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.constants.WebApi;
import com.nmpa.nmpaapp.http.OkHttpUtil;
import com.nmpa.nmpaapp.modules.abnormal.bean.ListPageBean;
import com.nmpa.nmpaapp.modules.huanxin.db.InviteMessgeDao;
import com.nmpa.nmpaapp.modules.huanxin.db.UserDao;
import com.nmpa.nmpaapp.modules.huanxin.domain.RemoteMessage;
import com.nmpa.nmpaapp.modules.huanxin.group.GroupsActivity;
import com.nmpa.nmpaapp.modules.huanxin.runtimepermissions.PermissionsManager;
import com.nmpa.nmpaapp.modules.huanxin.runtimepermissions.PermissionsResultAction;
import com.nmpa.nmpaapp.modules.huanxin.ui.BaseActivity;
import com.nmpa.nmpaapp.modules.huanxin.ui.ChatActivity;
import com.nmpa.nmpaapp.modules.huanxin.ui.ContactListFragment;
import com.nmpa.nmpaapp.modules.huanxin.ui.ConversationListFragment;
import com.nmpa.nmpaapp.modules.huanxin.ui.SettingsFragment;
import com.nmpa.nmpaapp.modules.huanxin.utils.Constant;
import com.nmpa.nmpaapp.modules.huanxin.utils.DemoHelper;
import com.nmpa.nmpaapp.modules.huanxin.utils.EMMessageUtils;
import com.nmpa.nmpaapp.modules.login.LoginActivity;
import com.nmpa.nmpaapp.utils.FileLoadUtils;
import com.nmpa.nmpaapp.utils.StringUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class MSNMainActivity extends BaseActivity {
    private static final String TAG = "MSNMainActivity";
    // textview for unread message count
    private TextView unreadLabel;
    // textview for unread event message
    private TextView unreadAddressLable;
    private Button[] mTabs;
    private ContactListFragment contactListFragment;
    private SettingsFragment settingFragment;
    private Fragment[] fragments;
    private int index;
    private int currentTabIndex;
    // user logged into another device
    public boolean isConflict = false;
    // user account was removed
    private boolean isCurrentAccountRemoved = false;
    private Context mContext;

    private ImageView iv_back;
    protected Handler handler = new Handler();

    /**
     * check if current user account was remove
     */
    public boolean getCurrentAccountRemoved() {
        return isCurrentAccountRemoved;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = MSNMainActivity.this;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    //some device doesn't has activity to handle this intent
                    //so add try catch
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                } catch (Exception e) {
                }
            }
        }
        
        //make sure activity will not in background if user is logged into another device or removed
        if (getIntent() != null &&
                (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false) ||
                        getIntent().getBooleanExtra(Constant.ACCOUNT_KICKED_BY_CHANGE_PASSWORD, false) ||
                        getIntent().getBooleanExtra(Constant.ACCOUNT_KICKED_BY_OTHER_DEVICE, false))) {
            DemoHelper.getInstance().logout(false,null);
            finish();
            Log.e(TAG,"LoginActivity");
			      startActivity(new Intent(this, LoginActivity.class));
            return;
        } else if (getIntent() != null && getIntent().getBooleanExtra("isConflict", false)) {
            Log.e(TAG,"1111   LoginActivity");
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        setContentView(R.layout.em_activity_main);
        // runtime permission for android 6.0, just require all permissions here for simple
        requestPermissions();

        initView();

        showExceptionDialogFromIntent(getIntent());
        inviteMessgeDao = new InviteMessgeDao(this);
        UserDao userDao = new UserDao(this);
        Map<String, EaseUser> m = DemoHelper.getInstance().getContactList();
        Log.e(TAG,"user.size="+m.size());
        getContactList(m);
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        Log.e(TAG,"conversations.size="+conversations.size());
        if (conversations.size() == 0) {
            getConversationList();
        }

        if (savedInstanceState != null) {
            EMLog.d(TAG, "get fragments from saveInstanceState");
            conversationListFragment = (ConversationListFragment) getSupportFragmentManager().getFragment(savedInstanceState, ConversationListFragment.class.getSimpleName());
            contactListFragment = (ContactListFragment) getSupportFragmentManager().getFragment(savedInstanceState, ContactListFragment.class.getSimpleName());
            settingFragment = (SettingsFragment) getSupportFragmentManager().getFragment(savedInstanceState, SettingsFragment.class.getSimpleName());
            fragments = new Fragment[]{conversationListFragment, contactListFragment, settingFragment};
            getSupportFragmentManager().beginTransaction()
                    .show(conversationListFragment)
                    .hide(contactListFragment)
                    .hide(settingFragment)
                    .commit();
        } else {
            conversationListFragment = new ConversationListFragment();
            contactListFragment = new ContactListFragment();
            settingFragment = new SettingsFragment();
            fragments = new Fragment[]{conversationListFragment, contactListFragment, settingFragment};

            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, conversationListFragment)
                    .add(R.id.fragment_container, contactListFragment).hide(contactListFragment)
                    .add(R.id.fragment_container, settingFragment).hide(settingFragment)
                    .show(conversationListFragment)
                    .commit();
        }

        //register broadcast receiver to receive the change of group from DemoHelper
        registerBroadcastReceiver();
        EMClient.getInstance().contactManager().setContactListener(new MyContactListener());
        EMClient.getInstance().addClientListener(clientListener);
        EMClient.getInstance().addMultiDeviceListener(new MyMultiDeviceListener());
        //debug purpose only
        registerInternalDebugReceiver();

        // 获取华为 HMS 推送 token
//		HMSPushHelper.getInstance().getHMSToken(this);
    }
    
    private void importDataBaseLast(List<EMMessage> messages) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                EMClient.getInstance().chatManager().importMessages(messages);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (conversationListFragment != null) {
                                for (int i=0;i<messages.size();i++) {
                                    EMMessage emMessage = messages.get(i);
                                    int chatType = 1;
                                    if (emMessage.getChatType() == EMMessage.ChatType.Chat) {
                                        chatType = 1;
                                        String userName = "";
                                        if (emMessage.getFrom().equals(DemoHelper.getInstance().getCurrentUsernName())) {
                                            userName = emMessage.getTo();
                                        } else {
                                            userName = emMessage.getFrom();
                                        }
                                        Log.e(TAG,"importMessages emMessage.getFrom()="+emMessage.getFrom());
                                        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(userName, EaseCommonUtils.getConversationType(chatType), true);
                                        conversation.markAllMessagesAsRead();
                                    } else {
                                        chatType = 2;
                                        Log.e(TAG,"importMessages emMessage.getTo()="+emMessage.getTo());
                                        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(emMessage.getTo(), EaseCommonUtils.getConversationType(chatType), true);
                                        conversation.markAllMessagesAsRead();
                                    }
                                }
                                conversationListFragment.refresh();
                            }
                        }
                    },3000);
                }
        }.execute();
    }
    
    private void getFile(EMMessage message,String type,String path, String fileName) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                downLoad(message,type,path,fileName);
                return null;
            }
            
            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                Log.e(TAG,"getFile"+result);
            }
        }.execute();
    }
    
    HttpURLConnection con;

    private void downLoad(EMMessage emMessage,String type,String path, String fileName) {
        try {
            URL url = new URL(path);
            con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(5000);
            con.setConnectTimeout(5000);
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200) {
                InputStream is = con.getInputStream();//获取输入流
                FileOutputStream fileOutputStream = null;//文件输出流
                if (is != null) {
                    String toUser = "";
                    if (emMessage.direct() == EMMessage.Direct.RECEIVE) {
                        toUser = emMessage.getFrom();
                    }
                    File file = FileLoadUtils.createFileEm(type,fileName,toUser);
                    fileOutputStream = new FileOutputStream(file);//指定文件保存路径，代码看下一步
                    byte[] buf = new byte[1024];
                    int ch;
                    while ((ch = is.read(buf)) != -1) {
                        fileOutputStream.write(buf, 0, ch);//将获取到的流写入文件中
                    }
                }
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                if (type.equals("4")) {
                    String thum = FileLoadUtils.getThumnailPath(emMessage.getFrom(),((EMVideoMessageBody)emMessage.getBody()).getLocalUrl());
                    ((EMVideoMessageBody)emMessage.getBody()).setLocalThumb(thum);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,"下载成功"+fileName,Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,"下载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //最后将conn断开连接
            if (con != null) {
                con.disconnect();
                con = null;
            }
        }
    }

    List<EaseUser> contactList = new ArrayList<EaseUser>();
    protected void getContactList(Map<String, EaseUser> contactsMap) {
        contactList.clear();
        if(contactsMap == null){
            return;
        }
        synchronized (contactsMap) {
            Iterator<Map.Entry<String, EaseUser>> iterator = contactsMap.entrySet().iterator();
            List<String> blackList = EMClient.getInstance().contactManager().getBlackListUsernames();
            while (iterator.hasNext()) {
                Map.Entry<String, EaseUser> entry = iterator.next();
                // to make it compatible with data in previous version, you can remove this check if this is new app
                if (!entry.getKey().equals("item_new_friends")
                        && !entry.getKey().equals("item_groups")
                        && !entry.getKey().equals("item_chatroom")
                        && !entry.getKey().equals("item_robots")){
                    if(!blackList.contains(entry.getKey())){
                        //filter out users in blacklist
                        EaseUser user = entry.getValue();
                        EaseCommonUtils.setUserInitialLetter(user);
                        contactList.add(user);
                    }
                }
            }
        }

    }
    
    List<EMMessage> lastEMMessages = new ArrayList<>();
    private void getConversationList() {
        List<EMGroup> groups = EMClient.getInstance().groupManager().getAllGroups();
        Log.e(TAG,"groups="+groups.size());
        String groupStr = "";
        for (int i=0;i<groups.size();i++) {
            if (i == groups.size()-1) {
                groupStr += groups.get(i).getGroupId();
            } else {
                groupStr += groups.get(i).getGroupId()+",";
            }
        }
        List<RemoteMessage> lastRemoteMsgs = new ArrayList<>();
        HashMap<String, Object> baseParam = new HashMap<>(4);
        baseParam.put("pageNo",1);
        baseParam.put("pageSize",10);
        baseParam.put("sendUser",DemoHelper.getInstance().getCurrentUsernName());
        baseParam.put("groups",groupStr);
        OkHttpUtil.post(TAG, WebApi.MSN_CONVERSION_LIST, baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG,"getConversationList e="+e);
            }
            
            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG,"getConversationList response="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getInt("code") == 200) {
                        JSONObject object1 = object.getJSONObject("data");
                        JSONArray array = object1.getJSONArray("list");
                        Gson gson = new Gson();
                        if (array != null && array.length() != 0) {
                            for (int i=0;i<array.length();i++) {
                                RemoteMessage remoteMessage = gson.fromJson(array.get(i).toString(), RemoteMessage.class);
                                Log.e(TAG,"getConversationList  array="+array.get(i).toString());
                                convertMsgs(remoteMessage,lastEMMessages);
                                lastRemoteMsgs.add(remoteMessage);
                            }
                            importDataBaseLast(lastEMMessages);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void convertMsgs(RemoteMessage remoteMessage,List<EMMessage> emMessages) {
        EMMessage message = null;
        if (remoteMessage.getMesType().equals("1")) {
            message = EMMessage.createTxtSendMessage(remoteMessage.getMessage(), remoteMessage.getReceiveUser());
        } else if (remoteMessage.getMesType().equals("2")) {
            message = EMMessageUtils.createLocationMsg(remoteMessage);
        } else if (remoteMessage.getMesType().equals("3")) {//语音
            message = createVoiceMsg(remoteMessage);
        } else if (remoteMessage.getMesType().equals("4")) {//视频
            message = EMMessageUtils.createVideoMsg(remoteMessage);
            getFile(message,remoteMessage.getMesType(),remoteMessage.getFileUrl(),((EMVideoMessageBody)message.getBody()).getFileName());
        } else if (remoteMessage.getMesType().equals("5")) {//图片
            message = createImageMsg(remoteMessage);
        } else if (remoteMessage.getMesType().equals("6")) {//文件
            message = createFileMsg(remoteMessage);
        } else if (remoteMessage.getMesType().equals("7")) {
            message = EMMessage.createTxtSendMessage(remoteMessage.getMessage(), remoteMessage.getReceiveUser());
            message.setAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL,true);
        } else if (remoteMessage.getMesType().equals("8")) {//视频电话
            message = EMMessage.createTxtSendMessage(remoteMessage.getMessage(), remoteMessage.getReceiveUser());
            message.setAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL,true);
        } else if (remoteMessage.getMesType().equals("9")) {//企业名片
            message = EMMessage.createTxtSendMessage(remoteMessage.getMessage(), remoteMessage.getReceiveUser());
            message.setAttribute("is_office",true);
        }
        if (remoteMessage.getSendUser().equals(DemoHelper.getInstance().getCurrentUsernName())) {
            message.setDirection(EMMessage.Direct.SEND);
        } else {
            message.setDirection(EMMessage.Direct.RECEIVE);
        }
        
        if (remoteMessage.getReceiveType().equals("0")) {
            message.setChatType(EMMessage.ChatType.Chat);
        } else {
            message.setChatType(EMMessage.ChatType.GroupChat);
        }

        message.setFrom(remoteMessage.getSendUser());
        message.setMsgId(remoteMessage.getMsgId());
        message.setMsgTime(Long.valueOf(remoteMessage.getSendTime()));
        message.setStatus(EMMessage.Status.SUCCESS);

        emMessages.add(message);
    }
    
    private EMMessage createVoiceMsg(RemoteMessage remoteMessage) {
        EMMessage message = null;
        int size = 0;
        if (!StringUtils.isEmpty(remoteMessage.getSize())) {
            size = Integer.valueOf(remoteMessage.getSize());
        }
        String msgStr = remoteMessage.getMessage();
        String[] msgArr = msgStr.split(",");
        String localurl="";
        for (int j=0;j<msgArr.length;j++) {
            String[] lat = msgArr[j].split(":");
            if (lat[0].contains("localurl")) {
                localurl = lat[1].trim();
            }
        }
        message = EMMessageUtils.createVoiceSendMessage(localurl, size, remoteMessage.getReceiveUser());
        ((EMVoiceMessageBody)message.getBody()).setRemoteUrl(remoteMessage.getFileUrl());
        getFile(message,remoteMessage.getMesType(),remoteMessage.getFileUrl(),((EMVoiceMessageBody)message.getBody()).getFileName());
        return message;
    }
    
    private EMMessage createImageMsg(RemoteMessage remoteMessage) {
        EMMessage message = null;
        String msgStr = remoteMessage.getMessage();
        String[] msgArr = msgStr.split(",");
        String localurl="";
        for (int j=0;j<msgArr.length;j++) {
            String[] lat = msgArr[j].split(":");
            if (lat[0].contains("localurl")) {
                localurl = lat[1].trim();
            }
        }
        message = EMMessageUtils.createImageSendMessage(remoteMessage.getSendUser(),localurl, false, remoteMessage.getReceiveUser());
        getFile(message,remoteMessage.getMesType(),remoteMessage.getFileUrl(),((EMImageMessageBody)message.getBody()).getFileName());
        ((EMImageMessageBody)message.getBody()).setRemoteUrl(remoteMessage.getFileUrl());
        return message;
    }

    private EMMessage createFileMsg(RemoteMessage remoteMessage) {
        EMMessage message = null;
        String msgStr = remoteMessage.getMessage();
        String[] msgArr = msgStr.split(",");
        String localurl="";
        for (int j=0;j<msgArr.length;j++) {
            String[] lat = msgArr[j].split(":");
            if (lat[0].contains("localUrl")) {
                localurl = lat[1].trim();
            }
        }
        
        message = EMMessageUtils.createFileSendMessage(remoteMessage.getSendUser(),localurl, remoteMessage.getReceiveUser());
        getFile(message,remoteMessage.getMesType(),remoteMessage.getFileUrl(),((EMFileMessageBody)message.getBody()).getFileName());
        ((EMFileMessageBody)message.getBody()).setRemoteUrl(remoteMessage.getFileUrl());
        return message;
    }
    
    EMClientListener clientListener = new EMClientListener() {
        @Override
        public void onMigrate2x(boolean success) {
            Toast.makeText(mContext, "onUpgradeFrom 2.x to 3.x " + (success ? "success" : "fail"), Toast.LENGTH_LONG).show();
            if (success) {
                refreshUIWithMessage();
            }
        }
    };
    
    @TargetApi(23)
    private void requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
//				Toast.makeText(MainActivity.this, "All permissions have been granted", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onDenied(String permission) {
                //Toast.makeText(MainActivity.this, "Permission " + permission + " has been denied", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * init views
     */
    private void initView() {
        EMLog.d(TAG, "initView");
        unreadLabel = (TextView) findViewById(R.id.unread_msg_number);
        unreadAddressLable = (TextView) findViewById(R.id.unread_address_number);
        mTabs = new Button[3];
        mTabs[0] = (Button) findViewById(R.id.btn_conversation);
        mTabs[1] = (Button) findViewById(R.id.btn_address_list);
        mTabs[2] = (Button) findViewById(R.id.btn_setting);
        // select first tab
        mTabs[0].setSelected(true);

        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * on tab clicked
     *
     * @param view
     */
     public void onTabClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_conversation:
                index = 0;
                break;
            case R.id.btn_address_list:
                index = 1;
                break;
            case R.id.btn_setting:
                index = 2;
                break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        mTabs[currentTabIndex].setSelected(false);
        // set current tab selected
        mTabs[index].setSelected(true);
        currentTabIndex = index;
    }

    EMMessageListener messageListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            for (EMMessage message: messages) {
                DemoHelper.getInstance().getNotifier().vibrateAndPlayTone(message);
            }
            refreshUIWithMessage();
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            refreshUIWithMessage();
        }
        
        @Override
        public void onMessageRead(List<EMMessage> messages) {
        }

        @Override
        public void onMessageDelivered(List<EMMessage> message) {
        }

        @Override
        public void onMessageRecalled(List<EMMessage> messages) {
            refreshUIWithMessage();
        }
        
        @Override
        public void onMessageChanged(EMMessage message, Object change) {}
    };

    private void refreshUIWithMessage() {
        runOnUiThread(new Runnable() {
            public void run() {
                // refresh unread count
                updateUnreadLabel();
                if (currentTabIndex == 0) {
                    // refresh conversation list
                    if (conversationListFragment != null) {
                        conversationListFragment.refresh();
                    }
                }
            }
        });
    }
    
    @Override
    public void back(View view) {
        super.back(view);
    }

    private void registerBroadcastReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_CONTACT_CHANAGED);
        intentFilter.addAction(Constant.ACTION_GROUP_CHANAGED);
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                updateUnreadLabel();
                updateUnreadAddressLable();
                if (currentTabIndex == 0) {
                    // refresh conversation list
                    if (conversationListFragment != null) {
                        conversationListFragment.refresh();
                    }
                } else if (currentTabIndex == 1) {
                    if(contactListFragment != null) {
                        contactListFragment.refresh();
                    }
                }
                
                String action = intent.getAction();
                if(action.equals(Constant.ACTION_GROUP_CHANAGED)){
                    if (EaseCommonUtils.getTopActivity(mContext).equals(GroupsActivity.class.getName())) {
                        GroupsActivity.instance.onResume();
                    }
                }
            }
        };
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    public class MyContactListener implements EMContactListener {
        @Override
        public void onContactAdded(String username) {
            Log.e("DemoHelp","onContactAdded  "+username);
        }
        @Override
        public void onContactDeleted(final String username) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (ChatActivity.activityInstance != null && ChatActivity.activityInstance.toChatUsername != null &&
                            username.equals(ChatActivity.activityInstance.toChatUsername)) {
                        String st10 = getResources().getString(R.string.have_you_removed);
                        Toast.makeText(MSNMainActivity.this, ChatActivity.activityInstance.getToChatUsername() + st10, Toast.LENGTH_LONG)
                                .show();
                        ChatActivity.activityInstance.finish();
                    }
                }
            });
            updateUnreadAddressLable();
        }
        
        @Override
        public void onContactInvited(String username, String reason) {
            Log.e("DemoHelp","onContactInvited  "+username+","+reason);
        }
        @Override
        public void onFriendRequestAccepted(String username) {
            Log.e("DemoHelp","onFriendRequestAccepted  "+username);
        }
        
        @Override
        public void onFriendRequestDeclined(String username) {
            Log.e("DemoHelp","onFriendRequestDeclined  "+username);
        }
    }

    public class MyMultiDeviceListener implements EMMultiDeviceListener {

        @Override
        public void onContactEvent(int event, String target, String ext) {

        }
        
        @Override
        public void onGroupEvent(int event, String target, final List<String> username) {
            switch (event) {
                case EMMultiDeviceListener.GROUP_LEAVE:
                    ChatActivity.activityInstance.finish();
                    break;
                default:
                    break;
            }
        }
    }
    
    private void unregisterBroadcastReceiver(){
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
        if (exceptionBuilder != null) {
            exceptionBuilder.create().dismiss();
            exceptionBuilder = null;
            isExceptionDialogShow = false;
        }
        unregisterBroadcastReceiver();

        try {
            unregisterReceiver(internalDebugReceiver);
        } catch (Exception e) {
        }

    }

    /**
     * update unread message count
     */
    public void updateUnreadLabel() {
        int count = getUnreadMsgCountTotal();
        if (count > 0) {
            unreadLabel.setText(String.valueOf(count));
            unreadLabel.setVisibility(View.VISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
        }
    }
    
    /**
     * update the total unread count
     */
    public void updateUnreadAddressLable() {
        runOnUiThread(new Runnable() {
            public void run() {
                int count = getUnreadAddressCountTotal();
                if (count > 0) {
                  unreadAddressLable.setVisibility(View.VISIBLE);
                } else {
                    unreadAddressLable.setVisibility(View.INVISIBLE);
                }
            }
        });

    }
    
    /**
     * get unread event notification count, including application, accepted, etc
     *
     * @return
     */
    public int getUnreadAddressCountTotal() {
        int unreadAddressCountTotal = 0;
        unreadAddressCountTotal = inviteMessgeDao.getUnreadMessagesCount();
        return unreadAddressCountTotal;
    }
    
    /**
     * get unread message count
     *
     * @return
     */
    public int getUnreadMsgCountTotal() {
        return EMClient.getInstance().chatManager().getUnreadMessageCount();
    }

    private InviteMessgeDao inviteMessgeDao;
    
    @Override
    protected void onResume() {
        super.onResume();

        if (!isConflict && !isCurrentAccountRemoved) {
            updateUnreadLabel();
            updateUnreadAddressLable();
        }
        // unregister this event listener when this activity enters the
        // background
        DemoHelper sdkHelper = DemoHelper.getInstance();
        sdkHelper.pushActivity(this);

        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG,"onPause");
        EMClient.getInstance().chatManager().removeMessageListener(messageListener);
        EMClient.getInstance().removeClientListener(clientListener);
        DemoHelper sdkHelper = DemoHelper.getInstance();
        sdkHelper.popActivity(this);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isConflict", isConflict);
        outState.putBoolean(Constant.ACCOUNT_REMOVED, isCurrentAccountRemoved);

        //save fragments
        FragmentManager fm = getSupportFragmentManager();
        for (Fragment f : fragments) {
            if (f.isAdded()) {
                fm.putFragment(outState, f.getClass().getSimpleName(), f);
            }
        }

        super.onSaveInstanceState(outState);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Log.e(TAG,"KeyEvent.KEYCODE_BACK");
//            moveTaskToBack(false);
//            return true;
//        }
        return super.onKeyDown(keyCode, event);
    }
    
    private android.app.AlertDialog.Builder exceptionBuilder;
    private boolean isExceptionDialogShow =  false;
    private BroadcastReceiver internalDebugReceiver;
    private ConversationListFragment conversationListFragment;
    //    private EaseConversationListFragment conversationListFragment;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;
    private int getExceptionMessageId(String exceptionType) {
        if(exceptionType.equals(Constant.ACCOUNT_CONFLICT)) {
            return R.string.connect_conflict;
        } else if (exceptionType.equals(Constant.ACCOUNT_REMOVED)) {
            return R.string.em_user_remove;
        } else if (exceptionType.equals(Constant.ACCOUNT_FORBIDDEN)) {
            return R.string.user_forbidden;
        }
        return R.string.Network_error;
    }
    /**
     * show the dialog when user met some exception: such as login on another device, user removed or user forbidden
     */
    private void showExceptionDialog(String exceptionType) {
        isExceptionDialogShow = true;
        DemoHelper.getInstance().logout(false,null);
        String st = getResources().getString(R.string.Logoff_notification);
        if (!MSNMainActivity.this.isFinishing()) {
            // clear up global variables
            try {
                if (exceptionBuilder == null)
                    exceptionBuilder = new android.app.AlertDialog.Builder(mContext);
                exceptionBuilder.setTitle(st);
                exceptionBuilder.setMessage(getExceptionMessageId(exceptionType));
                exceptionBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        exceptionBuilder = null;
                        isExceptionDialogShow = false;
                        finish();
						Intent intent = new Intent(mContext, LoginActivity.class);
                        Log.e(TAG,"2222   LoginActivity");
//                        Intent intent = new Intent(mContext, HuanxinIMDemoActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                exceptionBuilder.setCancelable(false);
                exceptionBuilder.create().show();
                isConflict = true;
            } catch (Exception e) {
                EMLog.e(TAG, "---------color conflictBuilder error" + e.getMessage());
            }
        }
    }
    
    private void showExceptionDialogFromIntent(Intent intent) {
        EMLog.e(TAG, "showExceptionDialogFromIntent=="+isExceptionDialogShow);
        if (!isExceptionDialogShow && intent.getBooleanExtra(Constant.ACCOUNT_CONFLICT, false)) {
            showExceptionDialog(Constant.ACCOUNT_CONFLICT);
        } else if (!isExceptionDialogShow && intent.getBooleanExtra(Constant.ACCOUNT_REMOVED, false)) {
            showExceptionDialog(Constant.ACCOUNT_REMOVED);
        } else if (!isExceptionDialogShow && intent.getBooleanExtra(Constant.ACCOUNT_FORBIDDEN, false)) {
            showExceptionDialog(Constant.ACCOUNT_FORBIDDEN);
        } else if (intent.getBooleanExtra(Constant.ACCOUNT_KICKED_BY_CHANGE_PASSWORD, false) ||
                intent.getBooleanExtra(Constant.ACCOUNT_KICKED_BY_OTHER_DEVICE, false)) {
            this.finish();
            startActivity(new Intent(this, LoginActivity.class));
            Log.e(TAG,"4444   LoginActivity");
//            startActivity(new Intent(this, HuanxinIMDemoActivity.class));
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showExceptionDialogFromIntent(intent);
    }

    /**
     * debug purpose only, you can ignore this
     */
     private void registerInternalDebugReceiver() {
        internalDebugReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                DemoHelper.getInstance().logout(false,new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                finish();
                                startActivity(new Intent(mContext, LoginActivity.class));
                                Log.e(TAG,"66666   LoginActivity");
//                                startActivity(new Intent(mContext, HuanxinIMDemoActivity.class));
                            }
                        });
                    }
                    @Override
                    public void onProgress(int progress, String status) {}

                    @Override
                    public void onError(int code, String message) {}
                });
            }
        };
        IntentFilter filter = new IntentFilter(getPackageName() + ".em_internal_debug");
        registerReceiver(internalDebugReceiver, filter);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }
}
