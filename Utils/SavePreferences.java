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
}
