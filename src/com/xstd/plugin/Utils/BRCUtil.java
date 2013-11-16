package com.xstd.plugin.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.xstd.plugin.receiver.ScreenBRC;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-16
 * Time: AM9:50
 * To change this template use File | Settings | File Templates.
 */
public class BRCUtil {

    public static void startHourAlarm(Context context) {
        cancelHourAlarm(context);
        Intent intent = new Intent();
        intent.setAction(ScreenBRC.HOUR_ALARM_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        long cur = System.currentTimeMillis();
        long firstTime = cur + ((long) 30) * 60 * 1000;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, firstTime, ((long) 30) * 60 * 1000, sender);
    }

    public static void cancelHourAlarm(Context context) {
        Intent intent = new Intent();
        intent.setAction(ScreenBRC.HOUR_ALARM_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

}
