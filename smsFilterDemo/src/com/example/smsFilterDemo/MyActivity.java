package com.example.smsFilterDemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.umeng.analytics.MobclickAgent;


public class MyActivity extends Activity {

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
}
