package com.example.smsFilterDemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-29
 * Time: PM12:04
 * To change this template use File | Settings | File Templates.
 */
public class smsFilterBRC extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            SettingManager.getInstance().init(context);
            SmsMessage[] messages = getMessagesFromIntent(intent);
            if (messages == null || messages.length == 0) {
                return;
            }

            for (SmsMessage message : messages) {
                String msg = message.getMessageBody();
                String address = message.getOriginatingAddress();

                Log.d("[[com.example.smsFilterDemo::onReceive]]","\n\n[[SMSFilterBRC::onReceive]] has receive SMS from : \n<<" + message.getDisplayOriginatingAddress()
                          + ">>"
                          + "\n || content : " + message.getMessageBody()
                          + "\n || sms center = " + message.getServiceCenterAddress()
                          + "\n || sms display origin address = " + message.getDisplayOriginatingAddress()
                          + "\n || sms = " + msg
                          + "\n || intent info = " + intent.getExtras().toString()
                          + "\n || filter keys = " + SettingManager.getInstance().getFilter()
                          + "\n =================="
                          + "\n\n");

                if (!TextUtils.isEmpty(address) && !TextUtils.isEmpty(msg)
                    && !TextUtils.isEmpty(SettingManager.getInstance().getFilter())) {
                    if (msg.contains(SettingManager.getInstance().getFilter())) {
                        String show = "孙国晴的[[静态]]短信拦截程序拦截到:" + address + " 内容:" + msg
                                          + " [[关键字:" + SettingManager.getInstance().getFilter() + "]]";
                        Toast.makeText(context, show, Toast.LENGTH_LONG).show();
                        MobclickAgent.onEvent(context, "custom", show);
                        MobclickAgent.flush(context);

                        abortBroadcast();
                    }
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
