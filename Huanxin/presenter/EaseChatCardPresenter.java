package com.hyphenate.easeui.widget.presenter;

import android.content.Context;
import android.widget.BaseAdapter;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowCard;
import com.hyphenate.exceptions.HyphenateException;

/**
 * Created by zhangsong on 17-10-12.
 */

public class EaseChatCardPresenter extends EaseChatRowPresenter {
    private static final String TAG = "EaseChatFilePresenter";
    @Override
    protected EaseChatRow onCreateChatRow(Context cxt, EMMessage message, int position, BaseAdapter adapter) {
        return new EaseChatRowCard(cxt, message, position, adapter);
    }

    @Override
    public void onBubbleClick(EMMessage message) {
        EMTextMessageBody fileMessageBody = (EMTextMessageBody) message.getBody();
        
        if (message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked() && message.getChatType() == EMMessage.ChatType.Chat) {
            try {
                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (HyphenateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
