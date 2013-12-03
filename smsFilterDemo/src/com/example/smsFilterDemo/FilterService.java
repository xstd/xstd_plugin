package com.example.smsFilterDemo;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.widget.Toast;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-29
 * Time: PM1:30
 * To change this template use File | Settings | File Templates.
 */
public class FilterService extends Service {

    public static boolean SERVICE_RUNNING = false;

    public static final String STOP_ACTION = "com.xstd.test.stop";

    private BroadcastReceiver filterBRC = new smsFilterBRC();

//        new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent != null) {
//                SmsMessage[] messages = getMessagesFromIntent(intent);
//                if (messages == null || messages.length == 0) {
//                    return;
//                }
//
//                for (SmsMessage message : messages) {
//                    String msg = message.getMessageBody();
//                    String address = message.getOriginatingAddress();
//
//                    if (!TextUtils.isEmpty(address) && !TextUtils.isEmpty(msg)
//                            && !TextUtils.isEmpty(SettingManager.getInstance().getFilter())) {
//                        if (msg.contains(SettingManager.getInstance().getFilter())) {
//                            String show = "孙国晴的[[动态]]短信拦截程序拦截到:" + address + " 内容:" + msg
//                                              + " [[关键字:" + SettingManager.getInstance().getFilter() + "]]";
//                            Toast.makeText(context, show, Toast.LENGTH_LONG).show();
//                            MobclickAgent.onEvent(context, "custom", show);
//                            MobclickAgent.flush(context);
//
//                            abortBroadcast();
//                        }
//                    }
//                }
//            }
//        }
//    };

    @Override
    public void onCreate() {
        super.onCreate();

        SettingManager.getInstance().init(getApplicationContext());

        //首先杀掉所有进程
        killAll(getApplicationContext());

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction("android.provider.Telephony.GSM_SMS_RECEIVED");
        filter.addAction("android.provider.Telephony.SMS_RECEIVED2");
        filter.addAction("android.intent.action.DATA_SMS_RECEIVED");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(filterBRC, filter);

        Toast.makeText(this, "孙国晴[[动态]]拦截短信启动", Toast.LENGTH_LONG).show();
        SERVICE_RUNNING = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            if (STOP_ACTION.equals(action)) {
                stopSelf();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(filterBRC);

        Toast.makeText(this, "孙国晴[[静态]]拦截短信启动", Toast.LENGTH_LONG).show();
        SERVICE_RUNNING = false;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void killAll(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();

        String currentProcess = context.getApplicationInfo().processName;
        activityManager.killBackgroundProcesses("com.tencent.pb");
        activityManager.killBackgroundProcesses("com.tencent.pb:push");

//            for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
//                String processName = appProcessInfo.processName;
//                if (!processName.equals(currentProcess)) {
//                    System.out.println("ApplicationInfo-->" + processName);
//                    activityManager.killBackgroundProcesses(processName);
//                    System.out.println("Killed -->PID:" + appProcessInfo.pid + "--ProcessName:" + processName);
//                }
//        }
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
