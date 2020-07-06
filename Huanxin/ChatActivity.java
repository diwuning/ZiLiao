package com.nmpa.nmpaapp.modules.huanxin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.util.EasyUtils;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.constants.WebApi;
import com.nmpa.nmpaapp.modules.huanxin.MSNMainActivity;
import com.nmpa.nmpaapp.modules.huanxin.runtimepermissions.PermissionsManager;
import com.nmpa.nmpaapp.utils.FileLoadUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * chat activity，EaseChatFragment was used {@link #EaseChatFragment}
 *
 */
public class ChatActivity extends BaseActivity{
    private static final String TAG = "ChatActivity";
    public static ChatActivity activityInstance;
    private EaseChatFragment chatFragment;
    public String toChatUsername;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.em_activity_chat);
        activityInstance = this;
        //get user id or group id
        toChatUsername = getIntent().getExtras().getString("userId");
        //use EaseChatFratFragment
        chatFragment = new ChatFragment();
        //pass parameters to chat fragment
        chatFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.container, chatFragment).commit();
        chatFragment.setOnItemClickListener(new EaseChatFragment.OnClickSendListener() {
            @Override
            public void onSendClick(EMMessage message) {
                sendRemoteMsg(message);
            }
        });
        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityInstance = null;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	// make sure only one chat activity is opened
        String username = intent.getStringExtra("userId");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }

    }
    
    @Override
    public void onBackPressed() {
        chatFragment.onBackPressed();
        if (EasyUtils.isSingleActivity(this)) {
            Intent intent = new Intent(this, MSNMainActivity.class);
            startActivity(intent);
        }
    }
    
    public String getToChatUsername(){
        return toChatUsername;
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }
    
    //向服务器发送消息
    private void sendRemoteMsg(EMMessage message) {
        Map<String, String> baseParam = new HashMap<>();
        baseParam.put("id","");
        baseParam.put("msgId",message.getMsgId());
        baseParam.put("sendUser",message.getFrom());
        baseParam.put("receiveUser",message.getTo());
        Log.e(TAG,"receiveType="+message.getChatType().name());
        String chatType ="";
        if (message.getChatType() == EMMessage.ChatType.Chat) {
            chatType = "0";
        } else {
            chatType = "1";
        }
        baseParam.put("receiveType",chatType);
        int type = 1;
        File file = null,file1 = null;
        int size = 0;
        Log.e(TAG,"type="+message.getType().name());
        if (message.getType().name().toString().equals("TXT")) {
            if (message.getBooleanAttribute("is_office",false)) {
                type = 9;
            } else {
                type = 1;
            }
            EMTextMessageBody emTextMessageBody = ((EMTextMessageBody) message.getBody());
            baseParam.put("message",emTextMessageBody.getMessage());
        } else if (message.getType().name().toString().equals("LOCATION")) {
            type = 2;
            baseParam.put("message",message.getBody().toString());
        } else if (message.getType().name().toString().equals("VOICE")) {
            type = 3;
            EMVoiceMessageBody emVoiceMessageBody = ((EMVoiceMessageBody) message.getBody());
            String filePath = emVoiceMessageBody.getLocalUrl();
            file = new File(filePath);
            baseParam.put("size",emVoiceMessageBody.getLength()+"");
            baseParam.put("message",message.getBody().toString());
        } else if (message.getType().name().toString().equals("VIDEO")) {
            type = 4;
            EMVideoMessageBody emVideoMessageBody = ((EMVideoMessageBody) message.getBody());
            String filePath = emVideoMessageBody.getLocalUrl();
            file = new File(filePath);
            baseParam.put("size",emVideoMessageBody.getDuration()+"");
            String thumbnail = emVideoMessageBody.getLocalThumb();
            Log.e(TAG,"thumbnail="+thumbnail+","+emVideoMessageBody.getLocalThumb()+",size="+emVideoMessageBody.getDuration()+","+emVideoMessageBody.getVideoFileLength());
            file1 = new File(thumbnail);
            baseParam.put("message",message.getBody().toString());
        } else if(message.getType().name().toString().equals("IMAGE")) {
            type = 5;
            String filePath = ((EMImageMessageBody) message.getBody()).getLocalUrl();
            file = new File(filePath);
            baseParam.put("message",message.getBody().toString());
        } else if (message.getType().name().toString().equals("FILE")) {
            type = 6;
            String filePath = ((EMFileMessageBody) message.getBody()).getLocalUrl();
            file = new File(filePath);
            baseParam.put("message",message.getBody().toString());
        } else {
            type = 1;
            baseParam.put("message",message.getBody().toString());
        }
        baseParam.put("mesType",String.valueOf(type));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        baseParam.put("sendTime",format.format(new Date(message.getMsgTime())));
        String url = "http://192.168.0.119:8080/nmpa/apis/appHx/save";
        Log.e(TAG,"baseParam="+ baseParam);

        FileLoadUtils.postFile(WebApi.MSN_CHAT_SAVE, baseParam, "mesFile", file,"thuFile",file1, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"sendRemoteMsg  e="+ e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = FileLoadUtils.getResponseBody(response);
                Log.e(TAG,"sendRemoteMsg  response="+ result+","+response);
            }
        });

    }
}
