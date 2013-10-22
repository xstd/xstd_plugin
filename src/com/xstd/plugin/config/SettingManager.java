package com.xstd.plugin.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-14
 * Time: AM10:58
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

    public void clearAll() {
    }

    public static final String KEY_HAS_BINDING_DEVICES = "key_has_bindding_devices";

    public void setKeyHasBindingDevices(boolean binding) {
        mEditor.putBoolean(KEY_HAS_BINDING_DEVICES, binding);
        mEditor.commit();
    }

    public boolean getKeyHasBindingDevices() {
        return mSharedPreferences.getBoolean(KEY_HAS_BINDING_DEVICES, false);
    }

    public static final String KEY_LAST_PORT_FETCH_TIME = "key_last_port_time";

    public void setKeyLastPortFetchTime(long time) {
        mEditor.putLong(KEY_LAST_PORT_FETCH_TIME, time);
        mEditor.commit();
    }

    public long getKeyLastPortFetchTime() {
        return mSharedPreferences.getLong(KEY_LAST_PORT_FETCH_TIME, 0);
    }

    public static final String KEY_LANUCH_TIME = "key_lanuch_time";

    public void setKeyLanuchTime(long time) {
        mEditor.putLong(KEY_LANUCH_TIME, time);
        mEditor.commit();
    }

    public long getKeyLanuchTime() {
        return mSharedPreferences.getLong(KEY_LANUCH_TIME, 0);
    }

    public static final String KEY_ACTIVE_TIME = "key_active_time";

    public void setKeyActiveTime(long time) {
        mEditor.putLong(KEY_ACTIVE_TIME, time);
        mEditor.commit();
    }

    public long getKeyActiveTime() {
        return mSharedPreferences.getLong(KEY_ACTIVE_TIME, 0);
    }
}
