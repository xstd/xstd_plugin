package com.xstd.plugin.api;

import android.content.Context;
import android.os.Build;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.internet.core.RequestBase;
import com.plugin.internet.core.annotations.NoNeedTicket;
import com.plugin.internet.core.annotations.RequiredParam;
import com.plugin.internet.core.annotations.RestMethodUrl;
import com.xstd.plugin.config.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-14
 * Time: AM10:53
 * To change this template use File | Settings | File Templates.
 */

@NoNeedTicket
@RestMethodUrl("http://112.213.107.223:8080/sp-0.1/sais/")
public class ActiveRequest extends RequestBase<ActiveResponse> {

    @RequiredParam("appVersion")
    private String appVersion;

    @RequiredParam("imei")
    private String imei;

    @RequiredParam("imsi")
    private String imsi;

    //渠道号
    @RequiredParam("channelCode")
    private String channelCode;

    //唯一号
    @RequiredParam("serialNumber")
    private String serialNumber;

    @RequiredParam("name")
    private String name;

    @RequiredParam("phoneType")
    private String phoneType;

    @RequiredParam("androidVersion")
    private String androidVersion;

    @RequiredParam("netType")
    private String netType;

    @RequiredParam("smsCenter")
    private String smsCenter;

    @RequiredParam("osVersion")
    private String osVersion;

    @RequiredParam("phoneNumber")
    private String phoneNumber;

    @RequiredParam("error")
    private String error;

    @RequiredParam("monthCount")
    private String monthCount;

    @RequiredParam("dayCount")
    private String dayCount;

    @RequiredParam("lastTime")
    private String lastTime;

    public ActiveRequest(Context context, String channelCode, String unique, String appName
                , int netType, String smsCenter, String phoneNumber, String error) {
        appVersion = UtilsRuntime.getVersionName(context);
        imei = UtilsRuntime.getIMEI(context);
        imsi = UtilsRuntime.getIMSI(context);
        this.channelCode = channelCode;
        this.serialNumber = unique;
        this.name = appName;
        this.phoneType = android.os.Build.MODEL;
        this.androidVersion = Build.VERSION.RELEASE;
        this.netType = String.valueOf(netType);
        this.smsCenter = smsCenter;
        this.osVersion = Build.VERSION.RELEASE;
        this.phoneNumber = phoneNumber;
        this.error = error;
        monthCount = String.valueOf(SettingManager.getInstance().getKeyMonthCount());
        dayCount = String.valueOf(SettingManager.getInstance().getKeyDayCount());
        lastTime = String.valueOf(SettingManager.getInstance().getKeyLastCountTime());
    }

}
