package com.googl.plugin.x;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import com.xstd.plugin.Utils.SMSUtil;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import android.content.ComponentName;

public class XSTDPluginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xstdplugin);

        View bind = findViewById(R.id.binding);
        bind.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                                              DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                   new ComponentName(getApplicationContext(), DeviceBindBRC.class));
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                   "（自定义区域2）");
//                startActivityForResult(intent, 1);
                startActivity(intent);

            }
        });

        View sms = findViewById(R.id.send_sms);
        sms.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                SMSUtil.sendSMS("15810864155", "测试数据，11YY");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.xstdplugin, menu);
        return true;
    }
}
