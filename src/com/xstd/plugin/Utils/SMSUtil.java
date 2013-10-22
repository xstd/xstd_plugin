package com.xstd.plugin.Utils;

import android.telephony.SmsManager;
import com.xstd.plugin.config.Config;

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

}
