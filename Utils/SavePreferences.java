package com.nmpa.nmpaapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.nmpa.nmpaapp.appmanager.LibApp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class SavePreferences {
    private static final String TAG = "SavePreferences";

    private static final String PREFERENCE_FILE = "app";
    
    /**
     * 提交信息
     *
     * @param editor 编好编辑器
     */
    public static void apply(SharedPreferences.Editor editor) {
        editor.apply();
    }

    /**
     * 加入到偏好设置
     *
     * @param name 存储键
     * @param data 存储值
     */
    public static void setData(String name, Object data) {
        try {
            SharedPreferences sp = LibApp.getContext().getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            if (data == null) {
                editor.putString(name, null);
            }
            if (data instanceof String) {
                editor.putString(name, data.toString());
            } else if (data instanceof Integer) {
                editor.putInt(name, Integer.parseInt(data.toString()));
            } else if (data instanceof Long) {
                editor.putLong(name, Long.parseLong(data.toString()));
            } else if (data instanceof Float) {
                editor.putFloat(name, Float.valueOf(data.toString()));
            } else if (data instanceof HashSet || data instanceof Set) {
                editor.putStringSet(name, (Set<String>) data);
            } else if (data instanceof Boolean) {
                editor.putBoolean(name, Boolean.valueOf(data.toString()));
            } else {
                setObject(name, data);
            }
            apply(editor);
        } catch (Exception ex) {
            LogUtil.e(TAG, "error:setData----:" + name);
        }
    }
    
    /**
     * 清除数据
     *
     * @param name key
     */
    public static void removeData(String name) {
        try {
            SharedPreferences sp = LibApp.getContext().getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(name);
            apply(editor);
        } catch (Exception ex) {
            LogUtil.e(TAG, "error:setData----:" + name);
        }
    }

    /**
     * 获取String类型
     *
     * @param name 存储键
     * @return String
     */
    public static String getString(String name) {
        try {
            SharedPreferences sp = LibApp.getContext().getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
            return sp.getString(name, "");
        } catch (Exception ex) {
            LogUtil.e(TAG, "error:getString----:" + name);
        }
        return null;
    }

    /**
     * 获取String类型
     *
     * @param name 存储键
     * @return String
     */
    public static String getString(String name, String defaultValue) {
        if (StringUtils.isNotEmpty(getString(name))) {
            return getString(name);
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取Int数据
     *
     * @param name 存储键
     * @return int
     */
    public static int getInt(String name) {
        try {
            SharedPreferences sp = LibApp.getContext().getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
            return sp.getInt(name, 0);
        } catch (Exception ex) {
            LogUtil.e(TAG, "error:getInt----:" + name);
        }
        return 0;
    }
    
    /**
     * 获取boolean 类型的数据
     *
     * @param name 存储键
     * @return Boolean
     */
    public static Boolean getBoolean(String name) {
        try {
            SharedPreferences sp = LibApp.getContext().getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
            return sp.getBoolean(name, true);
        } catch (Exception ex) {
            LogUtil.e(TAG, "error:getBoolean----:" + name);
        }
        return null;
    }

    /**
     * 获取boolean 类型的数据
     *
     * @param name 存储键
     * @return Boolean  默认返回值自己定
     */
    public static Boolean getBoolean(String name, boolean def) {
        try {
            SharedPreferences sp = LibApp.getContext().getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
            return sp.getBoolean(name, def);
        } catch (Exception ex) {
            LogUtil.e(TAG, "error:getBoolean----:" + name);
        }
        return def;
    }
    
    /**
     * 获取Long类型的数据
     *
     * @param name 存储键
     * @return long
     */
    public static Long getLong(String name) {
        try {
            SharedPreferences sp = LibApp.getContext().getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
            return sp.getLong(name, 0L);
        } catch (Exception ex) {
            LogUtil.e(TAG, "error:getLong----:" + name);
        }
        return null;
    }
    
    /**
     * 获取浮点类型
     *
     * @param name 存储键
     * @return float
     */
    public static Float getFloat(String name) {
        try {
            SharedPreferences sp = LibApp.getContext().getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
            return sp.getFloat(name, 0.00f);
        } catch (Exception ex) {
            LogUtil.e(TAG, "error:getFloat----:" + name);
        }
        return null;
    }
    
    /**
     * 获取Set<String>对象
     *
     * @param name 存储键
     * @return SetHighListview
     */
    public static Set getStringSet(String name) {
        try {
            SharedPreferences sp = LibApp.getContext().getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
            return sp.getStringSet(name, null);
        } catch (Exception ex) {
            LogUtil.e(TAG, "error:getStringSet----:" + name);
        }
        return null;
    }
    
    /**
     * 根据对象类型获取存储对象
     *
     * @param name 存储键
     * @param co   对象类型 如String.class
     * @return object
     */
    public static Object getData(String name, Class<?> co) {
        Object data = null;
        try {
            if (co == String.class) {
                data = getString(name);
            } else if (co == Integer.class) {
                data = getInt(name);
            } else if (co == Long.class) {
                data = getLong(name);
            } else if (co == Float.class) {
                data = getFloat(name);
            } else if (co == Set.class) {
                data = getStringSet(name);
            } else if (co == Boolean.class) {
                data = getBoolean(name);
            } else {
                data = getObject(name);
            }
        } catch (Exception ex) {
            LogUtil.e(TAG, "error:getData----:" + name);
        }
        return data;
    }
    
    /**
     * 保存object 对象到偏好设置
     *
     * @param key 名称
     * @param obj 存储的对象
     */
    public static void setObject(String key, Object obj) {
        try {
            SharedPreferences.Editor data = LibApp.getContext().getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE)
                    .edit();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(obj);
            String byteToHex = bytesToHexString(bos.toByteArray());
            data.putString(key, byteToHex);
            apply(data);
        } catch (IOException e) {
            LogUtil.e(TAG, "error:setObject----:" + key);
        }


    }

    /**
     * 从偏好设置中获取object对象
     *
     * @param key 存储的名称
     * @return 键值
     */
    public static Object getObject(String key) {
        try {
            SharedPreferences data = LibApp.getContext().getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
            if (data.contains(key)) {
                String value = data.getString(key, "");
                if (TextUtils.isEmpty(value)) {
                    return null;
                } else {
                    byte[] hexToByte = stringToBytes(value);
                    ByteArrayInputStream bis = new ByteArrayInputStream(hexToByte);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    Object obj = ois.readObject();
                    return obj;
                }
            }
        } catch (StreamCorruptedException e) {
            LogUtil.e(TAG, "error:getObject----:" + key);
            e.printStackTrace();
        } catch (IOException e) {
            LogUtil.e(TAG, "error:getObject----:" + key);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            LogUtil.e(TAG, "error:getObject----:" + key);
            e.printStackTrace();
        }

        return null;
    }
    
    /**
     * desc:将数组转为16进制
     *
     * @param bArray byte数组
     * @return String
     */
    private static String bytesToHexString(byte[] bArray) {
        if (bArray == null) {
            return null;
        }
        if (bArray.length == 0) {
            return "";
        }
        
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (byte aBArray : bArray) {
            sTemp = Integer.toHexString(0xFF & aBArray);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
    
    /**
     * desc:将16进制的数据转为数组
     *
     * @param data 要存储的字符串
     * @return byte数组
     */
    private static byte[] stringToBytes(String data) {
        String hexString = data.toUpperCase().trim();
        if (hexString.length() % 2 != 0) {
            return null;
        }
        byte[] retData = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i++) {
            int intCh;  // 两位16进制数转化后的10进制数
            char hexChar1 = hexString.charAt(i); //两位16进制数中的第一位(高位*16)
            int intCh1;
            if (hexChar1 >= '0' && hexChar1 <= '9') {
                intCh1 = (hexChar1 - 48) * 16;   //0 的Ascll - 48
            } else if (hexChar1 >= 'A' && hexChar1 <= 'F') {
                intCh1 = (hexChar1 - 55) * 16; // A 的Ascll - 65
            } else {
                return null;
            }
            i++;
            char hexChar2 = hexString.charAt(i); //两位16进制数中的第二位(低位)
            int intCh2;
            if (hexChar2 >= '0' && hexChar2 <= '9') {
                intCh2 = (hexChar2 - 48); // 0 的Ascll - 48
            } else if (hexChar2 >= 'A' && hexChar2 <= 'F') {
                intCh2 = hexChar2 - 55; // A 的Ascll - 65
            } else {
                return null;
            }
            intCh = intCh1 + intCh2;
            retData[i / 2] = (byte) intCh;//将转化后的数放入Byte里
        }
        return retData;
    }
}
