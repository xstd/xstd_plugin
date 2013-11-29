package com.example.smsFilterDemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-29
 * Time: PM1:21
 * To change this template use File | Settings | File Templates.
 */
public class SettingManager {

    private static SettingManager mInstance;

    private Context mContext;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences.Editor mEditor;

    public static synchronized SettingManager getInstance() {
        if (mInstance == null) {
            mInstance = new SettingManager();
        }

        return mInstance;
    }


    private static final String SHARE_PREFERENCE_NAME = "setting_manager_share_pref_custom";

    // 在Application中一定要调用
    public synchronized void init(Context context) {
        mContext = context.getApplicationContext();
        mSharedPreferences = mContext.getSharedPreferences(SHARE_PREFERENCE_NAME, 0);
        mEditor = mSharedPreferences.edit();
    }

    private SettingManager() {
    }

    public void setFilter(String msg) {
        mEditor.putString("filter", msg).commit();
    }

    public String getFilter() {
        return mSharedPreferences.getString("filter", "");
    }

    public void setServiceStart(boolean start) {
        mEditor.putBoolean("start", start).commit();
    }

    public boolean getServiceStart() {
        return mSharedPreferences.getBoolean("start", false);
    }
}
