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
