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
import com.nmpa.nmpaapp.router.Page;

import org.json.JSONException;
import org.json.JSONObject;

public class GePushIntentService extends GTIntentService {
    private static final String TAG = "GePushIntentService";
}
