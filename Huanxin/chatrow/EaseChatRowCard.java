package com.hyphenate.easeui.widget.chatrow;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.domain.OfficeCard;

public class EaseChatRowCard extends EaseChatRow{
    private static final String TAG = "EaseChatRowCard";

    protected TextView fileNameView;
	protected TextView fileSizeView;
    protected TextView fileStateView;
    protected TextView tvOfficeAddr;
    protected TextView tvOfficeTel;

    private EMTextMessageBody fileMessageBody;

    public EaseChatRowCard(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }
    
    @Override
    protected void onInflateView() {
        inflater.inflate(message.direct() == EMMessage.Direct.RECEIVE ?
                R.layout.ease_row_received_card : R.layout.ease_row_sent_card, this);
    }

    @Override
    protected void onFindViewById() {
        fileNameView = (TextView) findViewById(R.id.tv_file_name);
        fileSizeView = (TextView) findViewById(R.id.tv_file_size);
        fileStateView = (TextView) findViewById(R.id.tv_file_state);
        percentageView = (TextView) findViewById(R.id.percentage);
        tvOfficeAddr = (TextView) findViewById(R.id.tv_officeAddr);
        tvOfficeTel = (TextView) findViewById(R.id.tv_officeTel);
    }


    @Override
    protected void onSetUpView() {
        fileMessageBody = (EMTextMessageBody) message.getBody();
        String content = fileMessageBody.getMessage();
        Log.e(TAG, "content=" + content);
        if (message.getBooleanAttribute("is_office", false)) {
            String[] contentArr = content.split(",");
            OfficeCard card = new OfficeCard();
            card.setOfficeId(contentArr[0]);
            card.setOfficeName(contentArr[1]);
            card.setAddr(contentArr[2]);
            card.setTel(contentArr[3]);
            fileNameView.setText(card.getOfficeName());
            tvOfficeAddr.setText(card.getAddr());
            tvOfficeTel.setText(card.getTel());
        }
    }

    @Override
    protected void onViewUpdate(EMMessage msg) {
        switch (msg.status()) {
            case CREATE:
                onMessageCreate();
                break;
            case SUCCESS:
                onMessageSuccess();
                break;
            case FAIL:
                onMessageError();
                break;
            case INPROGRESS:
                onMessageInProgress();
                break;
        }
    }

    private void onMessageCreate() {
        progressBar.setVisibility(View.VISIBLE);
        if (percentageView != null)
            percentageView.setVisibility(View.INVISIBLE);
        if (statusView != null)
            statusView.setVisibility(View.INVISIBLE);
    }
    
    private void onMessageSuccess() {
        progressBar.setVisibility(View.INVISIBLE);
        if (percentageView != null)
            percentageView.setVisibility(View.INVISIBLE);
        if (statusView != null)
            statusView.setVisibility(View.INVISIBLE);
    }
    
    private void onMessageError() {
        progressBar.setVisibility(View.INVISIBLE);
        if (percentageView != null)
            percentageView.setVisibility(View.INVISIBLE);
        if (statusView != null)
            statusView.setVisibility(View.VISIBLE);
    }
    
    private void onMessageInProgress() {
        progressBar.setVisibility(View.VISIBLE);
        if (percentageView != null) {
            percentageView.setVisibility(View.VISIBLE);
            percentageView.setText(message.progress() + "%");
        }
        if (statusView != null)
            statusView.setVisibility(View.INVISIBLE);
    }
}
