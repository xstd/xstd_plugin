package com.xstd.plugin.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-18
 * Time: PM6:49
 * To change this template use File | Settings | File Templates.
 */
public class CommonUtil {

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
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
                    String id = prefs.getString(PREFS_DEVICE_ID, null);
                    if (id != null) {
                        // Use the ids previously computed and stored in the prefs file
                        uuid = UUID.fromString(id);
                    } else {
                        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        // Use the Android ID unless it's broken, in which case fallback on deviceId,
                        // unless it's not available, then fallback on a random number which we store
                        // to a prefs file
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
                        prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).commit();
                    }
                }
            }
        }

        return uuid;
    }

}
