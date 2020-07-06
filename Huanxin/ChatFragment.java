package com.nmpa.nmpaapp.modules.huanxin.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.model.EaseDingMessageHelper;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.easeui.ui.EaseChatFragment.EaseChatFragmentHelper;
import com.hyphenate.easeui.ui.EaseDingMsgSendActivity;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.chatrow.EaseCustomChatRowProvider;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenu;
import com.hyphenate.easeui.widget.presenter.EaseChatCardPresenter;
import com.hyphenate.easeui.widget.presenter.EaseChatRowPresenter;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.EasyUtils;
import com.hyphenate.util.PathUtil;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.constants.Const;
import com.nmpa.nmpaapp.constants.WebApi;
import com.nmpa.nmpaapp.http.OkHttpUtil;
import com.nmpa.nmpaapp.http.WebFrontUtil;
import com.nmpa.nmpaapp.modules.abnormal.bean.ListPageBean;
import com.nmpa.nmpaapp.modules.enforce.entity.OfficeBean;
import com.nmpa.nmpaapp.modules.huanxin.MSNMainActivity;
import com.nmpa.nmpaapp.modules.huanxin.conference.ConferenceActivity;
import com.nmpa.nmpaapp.modules.huanxin.domain.EmojiconExampleGroupData;
import com.nmpa.nmpaapp.modules.huanxin.domain.RemoteMessage;
import com.nmpa.nmpaapp.modules.huanxin.domain.RobotUser;
import com.nmpa.nmpaapp.modules.huanxin.group.GroupDetailsActivity;
import com.nmpa.nmpaapp.modules.huanxin.message.ForwardMessageActivity;
import com.nmpa.nmpaapp.modules.huanxin.utils.ChatRowConferenceInvitePresenter;
import com.nmpa.nmpaapp.modules.huanxin.utils.ChatRowLivePresenter;
import com.nmpa.nmpaapp.modules.huanxin.utils.Constant;
import com.nmpa.nmpaapp.modules.huanxin.utils.DemoHelper;
import com.nmpa.nmpaapp.modules.huanxin.utils.EMMessageUtils;
import com.nmpa.nmpaapp.modules.huanxin.utils.EaseChatRecallPresenter;
import com.nmpa.nmpaapp.modules.huanxin.utils.EaseChatVoiceCallPresenter;
import com.nmpa.nmpaapp.utils.FileLoadUtils;
import com.nmpa.nmpaapp.utils.SavePreferences;
import com.nmpa.nmpaapp.utils.StringUtils;
import com.nmpa.nmpaapp.utils.dialog.DialogManager;
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
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class ChatFragment extends EaseChatFragment implements EaseChatFragmentHelper {
    // constant start from 11 to avoid conflict with constant in base class
    private static final int ITEM_VIDEO = 11;
    private static final int ITEM_FILE = 12;
    private static final int ITEM_VOICE_CALL = 13;
    private static final int ITEM_VIDEO_CALL = 14;
    private static final int ITEM_CONFERENCE_CALL = 15;
    private static final int ITEM_LIVE = 16;
    private static final int ITEM_OFFICE_CARD = 17;
    
    private static final int REQUEST_CODE_SELECT_VIDEO = 11;
    private static final int REQUEST_CODE_SELECT_FILE = 12;
    private static final int REQUEST_CODE_GROUP_DETAIL = 13;
    private static final int REQUEST_CODE_CONTEXT_MENU = 14;
    private static final int REQUEST_CODE_SELECT_AT_USER = 15;
    
    private static final int MESSAGE_TYPE_SENT_VOICE_CALL = 1;
    private static final int MESSAGE_TYPE_RECV_VOICE_CALL = 2;
    private static final int MESSAGE_TYPE_SENT_VIDEO_CALL = 3;
    private static final int MESSAGE_TYPE_RECV_VIDEO_CALL = 4;
    private static final int MESSAGE_TYPE_CONFERENCE_INVITE = 5;
    private static final int MESSAGE_TYPE_LIVE_INVITE = 6;
    private static final int MESSAGE_TYPE_RECALL = 9;
    
    private static final int MESSAGE_TYPE_SEND_CARD = 14;
    private static final int MESSAGE_TYPE_RECV_CARD = 15;

    /**
     * if it is chatBot 
     */
    private boolean isRobot;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState,
                DemoHelper.getInstance().getModel().isMsgRoaming() && (chatType != EaseConstant.CHATTYPE_CHATROOM));
    }

    @Override
    protected boolean turnOnTyping() {
        return DemoHelper.getInstance().getModel().isShowMsgTyping();
    }
    EMMessage existMsg;
    
    @Override
    protected void setUpView() {
        setChatFragmentHelper(this);
        String userName = DemoHelper.getInstance().getCurrentUsernName();
        conversation = EMClient.getInstance().chatManager().getConversation(toChatUsername, EaseCommonUtils.getConversationType(chatType), true);
        Log.e(TAG,"conversation.getAllMsgCount()="+conversation.getAllMsgCount());
        List<EMMessage> emMessageList = conversation.getAllMessages();
        if (emMessageList.size() == 1) {
            existMsg = emMessageList.get(0);
        }
        
        if (conversation.getAllMsgCount() != 0 && conversation.getAllMsgCount() != 1) {
            setLisener();
        } else {
            conversation.clearAllMessages();
            conversation.clear();
            showWaitDialog("数据加载中……").show();
            getHistoryMsg(userName,toChatUsername);
        }

//        getHistoryMsg(userName,toChatUsername);

    }
    
    private void setLisener() {
        if (chatType == Constant.CHATTYPE_SINGLE) {
            Map<String, RobotUser> robotMap = DemoHelper.getInstance().getRobotList();
            if(robotMap!=null && robotMap.containsKey(toChatUsername)){
                isRobot = true;
            }
        }
        super.setUpView();
        // set click listener
        titleBar.setLeftLayoutClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (EasyUtils.isSingleActivity(getActivity())) {
                    Intent intent = new Intent(getActivity(), MSNMainActivity.class);
                    startActivity(intent);
                }
                onBackPressed();
            }
        });
        
        ((EaseEmojiconMenu)inputMenu.getEmojiconMenu()).addEmojiconGroup(EmojiconExampleGroupData.getData());
        if(chatType == EaseConstant.CHATTYPE_GROUP){
            inputMenu.getPrimaryMenu().getEditText().addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(count == 1 && "@".equals(String.valueOf(s.charAt(start)))){
                        Log.e(TAG,"onTextChanged   ");
//                        startActivityForResult(new Intent(getActivity(), PickAtUserActivity.class).
//                                putExtra("groupId", toChatUsername), REQUEST_CODE_SELECT_AT_USER);
                    }
                }
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }
                
                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        setOnRefresh1Listener(new OnRefresh1Listener() {
            @Override
            public boolean refresh(int page) {
                pageNo = page;
                getHistoryMsg(DemoHelper.getInstance().getCurrentUsernName(),toChatUsername);
                return false;
            }
        });
    }

    private boolean isLastPage = false;
    List<EMMessage> emMessages = new ArrayList<>();
    List<RemoteMessage> remoteMessages = new ArrayList<>();
    int pageNo = 1;
    
    private void getHistoryMsg(String sendUser, String receiveUser) {
        HashMap<String, Object> baseParam = new HashMap<>(4);
        baseParam.put("pageNo",pageNo);
        baseParam.put("pageSize",10);
        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            baseParam.put("sendUser",receiveUser);
            baseParam.put("receiveUser",receiveUser);
        } else {
            baseParam.put("sendUser",sendUser);
            baseParam.put("receiveUser",receiveUser);
        }

        OkHttpUtil.post(TAG, WebApi.MSN_CHAT_LIST, baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG,"getHistoryMsg  e="+ e);
            }
            
            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG,"getHistoryMsg  response="+ response);
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getInt("code") == 200) {
                        JSONObject object1 = object.getJSONObject("data");
                        Gson gson = new Gson();
                        if (pageNo == 1) {
                            remoteMessages.clear();
                        }
                        ListPageBean listPageBean = gson.fromJson(object1.toString(),ListPageBean.class);
                        if (listPageBean.isLastPage()) {
                            isLastPage = true;
                        } else {
                            isLastPage = false;
                        }
                        JSONArray array = object1.getJSONArray("list");
                        for (int i = 0;i<array.length();i++) {
                            RemoteMessage remoteMessage = gson.fromJson(array.get(i).toString(),RemoteMessage.class);
                            convertMsg(remoteMessage);
                        }
                        Log.e(TAG,"emMessages.size()="+emMessages.size());
                        if (emMessages.size()< 10) {
                            haveMoreData = false;
                        } else {
                            haveMoreData = true;
                        }
                        importDataBase(emMessages);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideWaitDialog();
                }
            }
        });
    }
    
    private void convertMsg(RemoteMessage remoteMessage) {
        EMMessage message = null;
        Log.e(TAG,"getHistoryMsg  Message="+ remoteMessage.getMessage()+","+remoteMessage.getSendTime()+","+remoteMessage.getCreateDate());
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
        } else if (remoteMessage.getMesType().equals("7")) {//语音
            message = EMMessage.createTxtSendMessage(remoteMessage.getMessage(), remoteMessage.getReceiveUser());
            message.setAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL,true);
        } else if (remoteMessage.getMesType().equals("8")) {//视频电话
            message = EMMessage.createTxtSendMessage(remoteMessage.getMessage(), remoteMessage.getReceiveUser());
            message.setAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL,true);
        } else if (remoteMessage.getMesType().equals("9")) {
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
//        message.setChatType(EMMessage.ChatType.Chat);
        message.setFrom(remoteMessage.getSendUser());
        message.setMsgId(remoteMessage.getMsgId());
        message.setMsgTime(Long.valueOf(remoteMessage.getSendTime()));
        message.setStatus(EMMessage.Status.SUCCESS);
        conversation.insertMessage(message);
//                            updateMsg(message);
        emMessages.add(message);
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

    private void updateMsg(EMMessage emMessage) {
        if (existMsg != null && emMessage.getMsgId().equals(existMsg.getMsgId())) {
            existMsg = emMessage;
//            EMClient.getInstance().chatManager().updateMessage(existMsg);
            conversation.updateMessage(existMsg);
        } else {
            conversation.insertMessage(emMessage);
        }
    }
    
    /*
    * 下载文件
    * */
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
                    Log.e(TAG,"downLoad  thum="+thum);
                    ((EMVideoMessageBody)emMessage.getBody()).setLocalThumb(thum);
                }
                Log.d(TAG,"下载成功");
            } else {
                Log.d(TAG,"下载失败");
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

    /*
    * 将数据导入数据库
    * */
    private void importDataBase(List<EMMessage> messages) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                EMClient.getInstance().chatManager().importMessages(emMessages);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setLisener();
                        hideWaitDialog();
                    }
                },3000);

                Log.e(TAG,"importMessages"+result);
            }
        }.execute();
    }
    
    @Override
    protected void registerExtendMenuItem() {
        //use the menu in base class
        super.registerExtendMenuItem();
        //extend menu items
        inputMenu.registerExtendMenuItem(R.string.attach_video, R.drawable.em_chat_video_selector, ITEM_VIDEO, extendMenuItemClickListener);
        inputMenu.registerExtendMenuItem(R.string.attach_file, R.drawable.em_chat_file_selector, ITEM_FILE, extendMenuItemClickListener);
        if(chatType == Constant.CHATTYPE_SINGLE){
            inputMenu.registerExtendMenuItem(R.string.attach_voice_call, R.drawable.em_chat_voice_call_selector, ITEM_VOICE_CALL, extendMenuItemClickListener);
            inputMenu.registerExtendMenuItem(R.string.attach_video_call, R.drawable.em_chat_video_call_selector, ITEM_VIDEO_CALL, extendMenuItemClickListener);
//            inputMenu.registerExtendMenuItem("企业名片", R.drawable.em_chat_video_call_selector, ITEM_OFFICE_CARD, extendMenuItemClickListener);
        } else if (chatType == Constant.CHATTYPE_GROUP) { // 音视频会议
            inputMenu.registerExtendMenuItem(R.string.voice_and_video_conference, R.drawable.em_chat_video_call_selector, ITEM_CONFERENCE_CALL, extendMenuItemClickListener);
//            inputMenu.registerExtendMenuItem(R.string.title_live, R.drawable.em_chat_video_call_selector, ITEM_LIVE, extendMenuItemClickListener);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
            switch (resultCode) {
            case ContextMenuActivity.RESULT_CODE_COPY: // copy
                clipboard.setPrimaryClip(ClipData.newPlainText(null, 
                        ((EMTextMessageBody) contextMenuMessage.getBody()).getMessage()));
                break;
            case ContextMenuActivity.RESULT_CODE_DELETE: // delete
                conversation.removeMessage(contextMenuMessage.getMsgId());
                messageList.refresh();
                // To delete the ding-type message native stored acked users.
                EaseDingMessageHelper.get().delete(contextMenuMessage);
                break;

            case ContextMenuActivity.RESULT_CODE_FORWARD: // forward
                Log.e(TAG,"ForwardMessageActivity");
                Intent intent = new Intent(getActivity(), ForwardMessageActivity.class);
                intent.putExtra("forward_msg_id", contextMenuMessage.getMsgId());
                startActivity(intent);
                break;
            case ContextMenuActivity.RESULT_CODE_RECALL://recall
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        EMMessage msgNotification = EMMessage.createTxtSendMessage(contextMenuMessage.getMsgId(),contextMenuMessage.getTo());
//                        EMTextMessageBody txtBody = new EMTextMessageBody(getResources().getString(R.string.msg_recall_by_self));
//                        msgNotification.addBody(txtBody);
//                        msgNotification.setMsgTime(contextMenuMessage.getMsgTime());
//                        msgNotification.setLocalTime(contextMenuMessage.getMsgTime());
//                        msgNotification.setAttribute(Constant.MESSAGE_TYPE_RECALL, true);
//                        msgNotification.setStatus(EMMessage.Status.SUCCESS);
//                        recalDelMsg(contextMenuMessage.getMsgId(),msgNotification);

                        try {
                            EMMessage msgNotification = EMMessage.createTxtSendMessage(" ",contextMenuMessage.getTo());
                            EMTextMessageBody txtBody = new EMTextMessageBody(getResources().getString(R.string.msg_recall_by_self));
                            msgNotification.addBody(txtBody);
                            msgNotification.setMsgTime(contextMenuMessage.getMsgTime());
                            msgNotification.setLocalTime(contextMenuMessage.getMsgTime());
                            msgNotification.setAttribute(Constant.MESSAGE_TYPE_RECALL, true);
                            msgNotification.setStatus(EMMessage.Status.SUCCESS);
                            EMClient.getInstance().chatManager().recallMessage(contextMenuMessage);
                            EMClient.getInstance().chatManager().saveMessage(msgNotification);
                            messageList.refresh();
                        } catch (final HyphenateException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();

                // Delete group-ack data according to this message.
                EaseDingMessageHelper.get().delete(contextMenuMessage);
                break;

            default:
                break;
            }
        }
        if(resultCode == Activity.RESULT_OK){
            switch (requestCode) {
            case REQUEST_CODE_SELECT_VIDEO: //send the video
                    if (data != null) {
                    int duration = data.getIntExtra("dur", 0);
                    String videoPath = data.getStringExtra("path");
                    File file = new File(PathUtil.getInstance().getImagePath(), "thvideo" + System.currentTimeMillis());
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        Bitmap ThumbBitmap = ThumbnailUtils.createVideoThumbnail(videoPath, 3);
                        ThumbBitmap.compress(CompressFormat.JPEG, 100, fos);
                        fos.close();
                        sendVideoMessage(videoPath, file.getAbsolutePath(), duration);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_CODE_SELECT_FILE: //send the file
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        sendFileByUri(uri);
                    }
                }
                break;
            case REQUEST_CODE_SELECT_AT_USER:
                if(data != null){
                    String username = data.getStringExtra("username");
                    inputAtUsername(username, false);
                }
                break;
            default:
                break;
            }
        }
        if (requestCode == REQUEST_CODE_GROUP_DETAIL) {
            Log.e(TAG,"requestCode="+requestCode);
            switch (resultCode) {
                case GroupDetailsActivity.RESULT_CODE_SEND_GROUP_NOTIFICATION:
                    // Start the ding-type msg send ui.
                    EMLog.i(TAG, "Intent to the ding-msg send activity.");
                    Intent intent = new Intent(getActivity(), EaseDingMsgSendActivity.class);
                    intent.putExtra(EaseConstant.EXTRA_USER_ID, toChatUsername);
                    startActivityForResult(intent, REQUEST_CODE_DING_MSG);
                    break;
            }
        }
    }

    private void recalDelMsg(String msgId,EMMessage msgNotification) {
        HashMap<String, Object> baseParam = new HashMap<>(4);
        baseParam.put("msgId", msgId);
        OkHttpUtil.post(TAG, WebApi.MSN_CHAT_DEL, baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG,"recalDelMsg e="+e);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG,"recalDelMsg response="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getInt("code") == 200) {
                        conversation.removeMessage(contextMenuMessage.getMsgId());
                        sendRecallMsg(msgNotification);
                        messageList.refresh();
//                        EaseDingMessageHelper.get().delete(contextMenuMessage);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendRecallMsg(EMMessage message) {
        if (message == null) {
            return;
        }
        
        Log.e(TAG,"sendMessage  username="+message.getUserName()+",to="+message.getTo()+",from="+message.getFrom()
                +",status="+message.status()+",direct="+message.direct()+",keySet="+message.ext().keySet()
                +",type="+message.getType()+",MsgId="+message.getMsgId()
                +",body="+message.getBody().toString());
        if(chatFragmentHelper != null){
            //set extension
            chatFragmentHelper.onSetMessageAttributes(message);
        }
        if (chatType == EaseConstant.CHATTYPE_GROUP){
            message.setChatType(EMMessage.ChatType.GroupChat);
        }else if(chatType == EaseConstant.CHATTYPE_CHATROOM){
            message.setChatType(EMMessage.ChatType.ChatRoom);
        }
        
        message.setMessageStatusCallback(messageStatusCallback);

        // Send message.
        EMClient.getInstance().chatManager().sendMessage(message);
//        if (onClickSendListener != null) {
//            onClickSendListener.onSendClick(message);
//        }
    }
    
    @Override
    public void onSetMessageAttributes(EMMessage message) {
        if(isRobot){
            //set message extension
            message.setAttribute("em_robot_message", isRobot);
        }
    }
    
    @Override
    public EaseCustomChatRowProvider onSetCustomChatRowProvider() {
        return new CustomChatRowProvider();
    }
    
    @Override
    public void onEnterToChatDetails() {
        Log.e(TAG,"onEnterToChatDetails chatType="+chatType);
        if (chatType == Constant.CHATTYPE_GROUP) {
            EMGroup group = EMClient.getInstance().groupManager().getGroup(toChatUsername);
            if (group == null) {
                Toast.makeText(getActivity(), R.string.gorup_not_found, Toast.LENGTH_SHORT).show();
                return;
            }
            startActivityForResult(
                    (new Intent(getActivity(), GroupDetailsActivity.class).putExtra("groupId", toChatUsername)),
                    REQUEST_CODE_GROUP_DETAIL);
        }else if(chatType == Constant.CHATTYPE_CHATROOM){
//        	startActivityForResult(new Intent(getActivity(), ChatRoomDetailsActivity.class).putExtra("roomId", toChatUsername), REQUEST_CODE_GROUP_DETAIL);
        }
    }
    
    @Override
    public void onAvatarClick(String username) {
        //handling when user click avatar
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
    
    @Override
    public void onAvatarLongClick(String username) {
        inputAtUsername(username);
    }
    
    @Override
    public boolean onMessageBubbleClick(EMMessage message) {
        //消息框点击事件，demo这里不做覆盖，如需覆盖，return true
        return false;
    }
    @Override
    public void onCmdMessageReceived(List<EMMessage> messages) {
        super.onCmdMessageReceived(messages);
    }
    
    @Override
    public void onMessageBubbleLongClick(EMMessage message) {
    	// no message forward when in chat room
        startActivityForResult((new Intent(getActivity(), ContextMenuActivity.class)).putExtra("message",message)
                .putExtra("ischatroom", chatType == EaseConstant.CHATTYPE_CHATROOM),
                REQUEST_CODE_CONTEXT_MENU);
    }
    
    @Override
    public boolean onExtendMenuItemClick(int itemId, View view) {
        switch (itemId) {
        case ITEM_VIDEO:
            Log.e(TAG,"onExtendMenuItemClick ITEM_VIDEO");
            Intent intent = new Intent(getActivity(), ImageGridActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
            break;
        case ITEM_FILE: //file
            Log.e(TAG,"onExtendMenuItemClick ITEM_FILE");
            selectFileFromLocal();
            break;
        case ITEM_VOICE_CALL:
            Log.e(TAG,"onExtendMenuItemClick ITEM_VOICE_CALL");
            startVoiceCall();
            break;
        case ITEM_VIDEO_CALL:
            Log.e(TAG,"onExtendMenuItemClick ITEM_VIDEO_CALL");
            startVideoCall();
            break;
        case ITEM_CONFERENCE_CALL:
            Log.e(TAG,"onExtendMenuItemClick ITEM_CONFERENCE_CALL");
            ConferenceActivity.startConferenceCall(getActivity(), toChatUsername);
            break;
        case ITEM_LIVE:
            Log.e(TAG,"onExtendMenuItemClick ITEM_LIVE");
//            LiveActivity.startLive(getContext(), toChatUsername);
            break;
        case ITEM_OFFICE_CARD:
            Log.e(TAG,"onExtendMenuItemClick ITEM_OFFICE_CARD");
            startOfficeCard();
            break;
        default:
            break;
        }
        //keep exist extend menu
        return false;
    }
    
    private void startOfficeCard() {
        HashMap<String, Object> baseParam = WebFrontUtil.getBaseParam();
        baseParam.put("id", SavePreferences.getString(Const.OFFICE_ID));
        OkHttpUtil.post(TAG, WebApi.PUNISH_OFFICE_DETAIL, baseParam, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG,"startOfficeCard e="+e);
            }
            
            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG,"startOfficeCard response="+response);
                try {
                    JSONObject object = new JSONObject(response);
                    Gson gson = new Gson();
                    if ((int)object.get("code") == 200) {
                        JSONObject object1 = object.getJSONObject("data");
                        OfficeBean officeBean = gson.fromJson(object1.toString(),OfficeBean.class);
                        String content = "officeId:"+officeBean.getId()+",公司名称:"+officeBean.getName()+",地址:"+officeBean.getAddr()+",联系电话:"+officeBean.getOfficeTel();
                        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
                        message.setAttribute("is_office",true);
                        sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * select file
     */
    protected void selectFileFromLocal() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }
    
    /**
     * make a voice call
     */
    protected void startVoiceCall() {
        if (!EMClient.getInstance().isConnected()) {
            Toast.makeText(getActivity(), R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG,"startVoiceCall");
            EMLog.i(TAG, "Intent to the ding-msg send activity.");
            startActivity(new Intent(getActivity(), VoiceCallActivity.class).putExtra("username", toChatUsername)
                    .putExtra("isComingCall", false));
            // voiceCallBtn.setEnabled(false);
            inputMenu.hideExtendMenuContainer();
        }
    }
    
    /**
     * make a video call
     */
    protected void startVideoCall() {
        if (!EMClient.getInstance().isConnected())
            Toast.makeText(getActivity(), R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
        else {
            Log.e(TAG,"startVideoCall");
            startActivity(new Intent(getActivity(), VideoCallActivity.class).putExtra("username", toChatUsername)
                    .putExtra("isComingCall", false));
            // videoCallBtn.setEnabled(false);
            inputMenu.hideExtendMenuContainer();
        }
    }
    
    /**
     * chat row provider 
     *
     */
    private final class CustomChatRowProvider implements EaseCustomChatRowProvider {
        @Override
        public int getCustomChatRowTypeCount() {
            //here the number is the message type in EMMessage::Type
        	//which is used to count the number of different chat row
            return 14;
        }

        @Override
        public int getCustomChatRowType(EMMessage message) {
            if(message.getType() == EMMessage.Type.TXT){
                //voice call
                if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)){
                    return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE_CALL : MESSAGE_TYPE_SENT_VOICE_CALL;
                }else if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false)){
                    //video call
                    return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO_CALL : MESSAGE_TYPE_SENT_VIDEO_CALL;
                }
                 //messagee recall
                else if(message.getBooleanAttribute(Constant.MESSAGE_TYPE_RECALL, false)){
                    return MESSAGE_TYPE_RECALL;
                } else if (!"".equals(message.getStringAttribute(Constant.MSG_ATTR_CONF_ID,""))) {
                    return MESSAGE_TYPE_CONFERENCE_INVITE;
                } else if (Constant.OP_INVITE.equals(message.getStringAttribute(Constant.EM_CONFERENCE_OP, ""))) {
                    return MESSAGE_TYPE_LIVE_INVITE;
                }else if (message.getBooleanAttribute("is_office",false)) {
                    return message.direct() == EMMessage.Direct.RECEIVE? MESSAGE_TYPE_RECV_CARD: MESSAGE_TYPE_SEND_CARD;
                }
            }
            return 0;
        }

        @Override
        public EaseChatRowPresenter getCustomChatRow(EMMessage message, int position, BaseAdapter adapter) {
            if(message.getType() == EMMessage.Type.TXT){
                // voice call or video call
                if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false) ||
                    message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false)){
                    EaseChatRowPresenter presenter = new EaseChatVoiceCallPresenter();
                    return presenter;
                }
                if (message.getBooleanAttribute("is_office",false)) {
                    Log.e(TAG,"getCustomChatRow  is_office");
                    EaseChatRowPresenter presenter = new EaseChatCardPresenter();
                    return presenter;
                }
                //recall message
                else if(message.getBooleanAttribute(Constant.MESSAGE_TYPE_RECALL, false)){
                    EaseChatRowPresenter presenter = new EaseChatRecallPresenter();
                    return presenter;
                } else if (!"".equals(message.getStringAttribute(Constant.MSG_ATTR_CONF_ID,""))) {
                    return new ChatRowConferenceInvitePresenter();
                } else if (Constant.OP_INVITE.equals(message.getStringAttribute(Constant.EM_CONFERENCE_OP, ""))) {
                    return new ChatRowLivePresenter();
                }
            }
            return null;
        }

    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private Dialog mWaitDialog;
    public Dialog showWaitDialog(String text) {
        if (mWaitDialog == null) {
            mWaitDialog = DialogManager.getWaitDialog(getActivity(), text);
        }
        if (mWaitDialog != null) {
            TextView textView = mWaitDialog.findViewById(R.id.tv_message);
            textView.setText(text);
            mWaitDialog.show();
        }
        return mWaitDialog;
    }

    public void hideWaitDialog() {
        try {
            if (mWaitDialog != null) {
                mWaitDialog.dismiss();
            }
            mWaitDialog = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
