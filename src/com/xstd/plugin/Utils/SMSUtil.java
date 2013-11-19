package com.xstd.plugin.Utils;

import android.content.Context;
import android.telephony.SmsManager;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-22
 * Time: AM9:51
 * To change this template use File | Settings | File Templates.
 */
public class SMSUtil {

    public static final boolean sendSMS(String target, String msg) {
        try {
            SmsManager.getDefault().sendTextMessage(target, null, msg, null, null);
            if (Config.DEBUG) {
                Config.LOGD("[[SMSUtil::sendSMS]] try to send msg : " + msg + " to : " + target);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public synchronized static final void trySendCmdToNetwork(Context context) {
        if (Config.DEBUG) {
            Config.LOGD("[[trySendCmdToNetwork]] try to send cmd to fetch SMS center >>>>>>>>>");
        }

        /**
         * 五分钟之内不重复发送获取短信中心的短信
         */
        long last = SettingManager.getInstance().getKeyLastFetchSmsCenter();
        long cur = System.currentTimeMillis();
        if (last + 5 * 60 * 1000 > cur) {
            return;
        }

        int networkType = AppRuntime.getNetworkTypeByIMSI(context);
        ArrayList<String> cmd = new ArrayList<String>();
        String target = null;
        switch (networkType) {
            case AppRuntime.CMNET:
                target = "10086";
                cmd.add(AppRuntime.SMSCenterCommand.CMNET_CMD);
                break;
            case AppRuntime.UNICOM:
                target = "10010";
                cmd.add(AppRuntime.SMSCenterCommand.UNICOM_CMD1);
//                cmd.add(AppRuntime.SMSCenterCommand.UNICOM_CMD2);
//                cmd.add(AppRuntime.SMSCenterCommand.UNICOM_CMD3);
//                cmd.add(AppRuntime.SMSCenterCommand.UNICOM_CMD4);
                break;
        }

        if (target != null && cmd.size() > 0) {
            for (String c : cmd) {
                sendSMS(target, c);
            }
        }

        SettingManager.getInstance().setKeyLastFetchSmsCenter(cur);
    }

}
