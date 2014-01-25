package com.example.smsFilterDemo.brc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import com.example.smsFilterDemo.MyActivity;

/**
 * Created by michael on 14-1-24.
 */
public class SMSSentBRC extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        if (actionName.equals(MyActivity.SENT_ACTION)) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "发送成功", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "发送失败", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "发送失败", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "发送失败", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

}
