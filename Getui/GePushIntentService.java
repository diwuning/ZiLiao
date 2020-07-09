package com.nmpa.nmpaapp.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import com.google.gson.Gson;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.modules.notification.NotificationBean;
import com.nmpa.nmpaapp.modules.notification.NotificationData;
import com.nmpa.nmpaapp.modules.notification.NotificationMsgActivity;
import com.nmpa.nmpaapp.modules.notification.PushPlayActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class GePushIntentService extends GTIntentService {
    private static final String TAG = "GePushIntentService";
    @Override
    public void onReceiveServicePid(Context context, int i) {

    }

    @Override
    public void onReceiveClientId(Context context, String s) {
        Log.e(TAG, "onReceiveClientId -> " + "clientid = " + s);
    }
    
    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage gtTransmitMessage) {
        Log.e(TAG, "onReceiveMessageData -> "  + gtTransmitMessage.getPayload());
        byte[] payload = gtTransmitMessage.getPayload();
        if (payload != null) {
            String payloadStr = new String(payload);
            try {
                JSONObject object = new JSONObject(payloadStr);
                NotificationBean notificationBean = new Gson().fromJson(object.toString(), NotificationBean.class);
                Log.d(TAG,object.toString());
                PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                if (pm != null && !pm.isScreenOn()) {
                    //点亮屏幕
                    @SuppressLint("InvalidWakeLockTag")
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
                    wl.acquire();
                    wl.release();
                }

                NotificationData data = notificationBean.getData();
                if (notificationBean != null && notificationBean.getType().equals("6")) {
                    if (data.getIsOpen() == 1) {
                        showVideoNotify(notificationBean);
                    } else {
                        showPopNotify(notificationBean);
                    }
                } else {
                    showPopNotify(notificationBean);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void showVideoNotify(NotificationBean notificationAlarmBean) {
        int num = 1;
        Intent intent = new Intent(this,PushPlayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);  //保证多个nitifition 跳转同一界面 不会显示多个
        intent.putExtra("url",notificationAlarmBean.getData().getUrl());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, num, intent, PendingIntent.FLAG_UPDATE_CURRENT);   //想要不同的 Pending intent 就要不同的 num 参参数
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        if (Build.VERSION.SDK_INT >= 26) {   //解决 sdk 26 版本之上notifices 不显示的问题
            NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            
            Notification.Builder notification = new Notification.Builder(this,"channel_id");
            notification
                    .setSmallIcon(R.drawable.spalsh_logo)//设置小图标
                    .setContentTitle(notificationAlarmBean.getTitle())
                    .setContentText(notificationAlarmBean.getTitle())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
            notification.build().flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(num,notification.build());
        } else{
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.spalsh_logo)//设置小图标
                    .setContentTitle(notificationAlarmBean.getTitle())
                    .setContentText(notificationAlarmBean.getTitle())
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build();
            notificationManager.notify(num, notification);    //想要不同的 notifice 就要不同的num 参数
        }
    }

    private void showPopNotify(NotificationBean notificationAlarmBean) {
        int num = 1;
//        int num = (int) (Math.random() * 100);
        Intent intent = new Intent(this, NotificationMsgActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);  //保证多个nitifition 跳转同一界面 不会显示多个
        intent.putExtra("notificationBean", notificationAlarmBean);
//        putString("title",notificationBean.getTitle());
        intent.putExtra("title", notificationAlarmBean.getTitle());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, num, intent, PendingIntent.FLAG_UPDATE_CURRENT);   //想要不同的 Pending intent 就要不同的 num 参参数
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {   //解决 sdk 26 版本之上notifices 不显示的问题
            NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            
            Notification.Builder notification = new Notification.Builder(this,"channel_id");
            notification
                    .setSmallIcon(R.drawable.spalsh_logo)//设置小图标
                    .setContentTitle(notificationAlarmBean.getTitle())
                    .setContentText(notificationAlarmBean.getTitle())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
            notification.build().flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(num,notification.build());
        } else{
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.spalsh_logo)//设置小图标
                    .setContentTitle(notificationAlarmBean.getTitle())
                    .setContentText(notificationAlarmBean.getTitle())
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build();
            notificationManager.notify(num, notification);    //想要不同的 notifice 就要不同的num 参数
        }
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean b) {

    }
    
    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {

    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage gtNotificationMessage) {
        Log.e(TAG, "onNotificationMessageArrived -> "  + gtNotificationMessage.getContent());
    }
    
    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage gtNotificationMessage) {

    }
}
