package com.nmpa.nmpaapp.modules.huanxin.utils;

import android.util.Log;

import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.nmpa.nmpaapp.modules.huanxin.domain.RemoteMessage;
import com.nmpa.nmpaapp.utils.FileLoadUtils;
import com.nmpa.nmpaapp.utils.StringUtils;

import java.io.File;
import java.io.IOException;

public class EMMessageUtils {
    private static final String TAG = "EMMessageUtils";

    public static EMMessage createImageSendMessage(String fromUser, String var0, boolean var1, String var2) {
        File file = new File(var0);
        String path = "";
        if (!file.exists()) {
            String fileName = file.getName();
            path = FileLoadUtils.BASE_PATH + fromUser + "/image/"+fileName;
            file = new File(path);
        } else {
            path = var0;
        }
        EMMessage var3 = EMMessage.createSendMessage(EMMessage.Type.IMAGE);
        var3.setTo(var2);
        EMImageMessageBody var4 = new EMImageMessageBody(file);
        var4.setSendOriginalImage(var1);
        var3.addBody(var4);
        return var3;
    }

    public static EMMessage createVoiceSendMessage(String var0, int var1, String var2) {
        File file = new File(var0);
        File pathFile = new File(file.getParent());
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        EMMessage var3 = EMMessage.createSendMessage(EMMessage.Type.VOICE);
        EMVoiceMessageBody var4 = new EMVoiceMessageBody(file, var1);
        var3.addBody(var4);
        var3.setTo(var2);
        return var3;
//        }
    }
    
    public static EMMessage createVideoSendMessage(String fromUser,String var0, String var1, int var2, String var3) {
        File file = new File(var0);
        String path = "";
        if (!file.exists()) {
            String fileName = file.getName();
            path = FileLoadUtils.BASE_PATH + fromUser + "/video/"+fileName;
            file = new File(path);
        } else {
            path = var0;
        }
        Log.e(TAG,"createVideoSendMessage  path="+path);
        EMMessage var5 = EMMessage.createSendMessage(EMMessage.Type.VIDEO);
        var5.setTo(var3);
        EMVideoMessageBody var6 = new EMVideoMessageBody(path, var1, var2, file.length());
        var5.addBody(var6);
        return var5;
    }
    
    public static EMMessage createFileSendMessage(String fromUser,String var0, String var1) {
        File file = new File(var0);
        Log.e(TAG,"createFileSendMessage  var0="+var0);
        String path = "";
        if (!file.exists()) {
            String fileName = file.getName();
            path = FileLoadUtils.BASE_PATH + fromUser + "/file/"+fileName;
            file = new File(path);
        } else {
            path = var0;
        }
        
        EMMessage var3 = EMMessage.createSendMessage(EMMessage.Type.FILE);
        var3.setTo(var1);
        EMNormalFileMessageBody var4 = new EMNormalFileMessageBody(file);
        var3.addBody(var4);
        return var3;
    }
    
    public static EMMessage createVideoMsg(RemoteMessage remoteMessage) {
        EMMessage message = null;
        int size = 0;
        if (!StringUtils.isEmpty(remoteMessage.getSize())) {
            size = Integer.valueOf(remoteMessage.getSize());
        }
        String msgStr = remoteMessage.getMessage();
        String[] msgArr = msgStr.split(",");
        String localurl="";
        String thumbnailUrl= "";
        for (int j=0;j<msgArr.length;j++) {
            String[] lat = msgArr[j].split(":");
            if (lat[0].contains("localUrl")) {
                localurl = lat[1].trim();
            } else if (lat[0].equals("thumbnailUrl")) {
                if (lat.length>1) {
                    thumbnailUrl = lat[1].trim();
                }
            }
        }
        
        message = createVideoSendMessage(remoteMessage.getSendUser(),localurl, remoteMessage.getThuImg(), size, remoteMessage.getReceiveUser());
        ((EMVideoMessageBody)message.getBody()).setRemoteUrl(remoteMessage.getFileUrl());
        return message;
    }

    public static EMMessage createLocationMsg(RemoteMessage remoteMessage) {
        EMMessage message = null;
        String locStr = remoteMessage.getMessage();
        String[] locArr = locStr.split(",");
        double latitude=0,longitude=0;
        String locationAddress="";
        for (int j=0;j<locArr.length;j++) {
            String[] lat = locArr[j].split(":");
            if (lat[0].equals("location")) {
                locationAddress = lat[1];
            } else if (lat[0].equals("lat")) {
                latitude = Double.valueOf(lat[1]);
            } else if (lat[0].equals("lng")) {
                longitude = Double.valueOf(lat[1]);
            }
        }
        message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, remoteMessage.getReceiveUser());
        return message;
    }
}
