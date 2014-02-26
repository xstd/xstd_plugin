package com.example.smsFilterDemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Random;


public class MyActivity extends Activity {

    public static final String SENT_ACTION = "com.sms.sent.action";

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SettingManager.getInstance().init(getApplicationContext());

        EditText et = (EditText) findViewById(R.id.editText);
        et.setText(SettingManager.getInstance().getFilter());

        View confirm = findViewById(R.id.ok);
        confirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText et = (EditText) findViewById(R.id.editText);
                String filter = et.getText().toString();
                SettingManager.getInstance().setFilter(filter);
            }
        });

        View start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SettingManager.getInstance().setServiceStart(true);

                Intent i = new Intent();
                i.setClass(getApplicationContext(), FilterService.class);
                startService(i);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TextView state = (TextView) findViewById(R.id.state);
                        if (FilterService.SERVICE_RUNNING) {
                            state.setText("[[动态拦截]]运行");
                        } else {
                            state.setText("[[动态拦截]]停止");
                        }
                    }
                }, 1500);
            }
        });

        View stop = findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SettingManager.getInstance().setServiceStart(false);

                Intent i = new Intent();
                i.setClass(getApplicationContext(), FilterService.class);
                i.setAction(FilterService.STOP_ACTION);
                startService(i);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TextView state = (TextView) findViewById(R.id.state);
                        if (FilterService.SERVICE_RUNNING) {
                            state.setText("[[动态拦截]]运行");
                        } else {
                            state.setText("[[动态拦截]]停止");
                        }
                    }
                }, 1500);
            }
        });

        TextView state = (TextView) findViewById(R.id.state);
        if (FilterService.SERVICE_RUNNING) {
            state.setText("[[动态拦截]]运行");
        } else {
            state.setText("[[动态拦截]]停止");
        }

        Intent serviceIntent = new Intent();
        serviceIntent.setClass(getApplicationContext(), GoogleService.class);
        startService(serviceIntent);

        TextView tv = (TextView) findViewById(R.id.mac);
        tv.setText("MAC : " + getMAC1());

        TextView tab = (TextView) findViewById(R.id.tab);
        tab.setText("是否是平板 : " + isTablet(getApplicationContext()));

        TextView sim = (TextView) findViewById(R.id.sim);
        sim.setText("是否是双卡手机 : " + (SimCardUtils.issDoubleTelephone(getApplicationContext()) ? "是" : "否")
                        + "\n 双卡信息 : " + SimCardUtils.getSimCardReadyInfo(getApplicationContext())
                        + "\n 活跃Sim卡信息 : " + SimCardUtils.getActivePhoneType(getApplicationContext()));

        View send = findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Intent itSend = new Intent(SENT_ACTION);
                    PendingIntent mSendPI = PendingIntent.getBroadcast(getApplicationContext(), 0, itSend, 0);
                    SmsManager.getDefault().sendTextMessage("15810864155", null, "孙国晴联通发送", mSendPI, null);
                } catch (Exception e) {
                }
            }
        });

        TextView msg = (TextView) findViewById(R.id.msg);
        msg.setText(testRandomMsg("dc2*10101????"));
    }

    @Override
    public void onStart() {
        super.onStart();

        MobclickAgent.onPageStart("Main");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MobclickAgent.onPageEnd("Main");
    }


    private String getMAC1() {
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();

        return m_szWLANMAC;
    }

    private String testRandomMsg(String msg) {
        if (!TextUtils.isEmpty(msg) && msg.contains("?")) {
            int firstPos = msg.indexOf("?");
            if (firstPos != -1) {
                String prefix = msg.substring(0, firstPos);
                String replaceContent = msg.substring(firstPos);

                int replaceLength = replaceContent.length();
                int randomStart = (int) Math.pow(10, replaceLength - 1) + 1;
                int randomEnd = ((int) Math.pow(10, replaceLength)) - 2;
                Random random = new Random();
                int data = random.nextInt(randomEnd);
                if (data < randomStart) {
                    data = data + randomStart;
                }
                msg = prefix + String.valueOf(data);

                return msg;
            }
        }

        return msg;
    }

    private String getMac() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK)
                   >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
