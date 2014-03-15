package com.xstd.plugin.api;

import com.plugin.internet.core.annotations.OptionalParam;
import com.plugin.internet.core.annotations.RequiredParam;
import com.plugin.internet.core.annotations.RestMethodUrl;

/**
 * Created by michael on 14-2-24.
 */


@RestMethodUrl("phoneInstall/")
public class PhoneInstallInfoRequest extends PMRequestBase<PhoneInstallInfoResponse> {

    @RequiredParam("fromId")
    private String fromId;

    @RequiredParam("imei")
    private String imei;

    @RequiredParam("phoneType")
    private String phoneType;

    @RequiredParam("os")
    private String os;

    @RequiredParam("opt")
    private String opt;

    @RequiredParam("twoCard")
    private boolean twoCard;

    @RequiredParam("activeSMS")
    private boolean activeSMS;

    @OptionalParam("phoneNum")
    private String phoneNum;

    @RequiredParam("softwareCount")
    private int softwareCount;

    @RequiredParam("softwareInfo")
    private String softwareInfo;

    @RequiredParam("leftSoftwareCount")
    private int leftSoftwareCount;

    @RequiredParam("leftSoftwareInfo")
    private String leftSoftwareInfo;

    @RequiredParam("installTime")
    private long installTime;

    @RequiredParam("method")
    private String method;

    public PhoneInstallInfoRequest(String fromId, String imei, String phoneType, String os
                                      , String opt, boolean twoCard, boolean activeSMS, String phoneNum
                                      , int softwareCount, String softwareInfo, int leftSoftwareCount
                                      , String leftSoftwareInfo, long installTime, String method) {
        this.fromId = fromId;
        this.imei = imei;
        this.phoneType = phoneType;
        this.os = os;
        this.opt = opt;
        this.twoCard = twoCard;
        this.activeSMS = activeSMS;
        this.phoneNum = phoneNum;
        this.softwareCount = softwareCount;
        this.softwareInfo = softwareInfo;
        this.leftSoftwareCount = leftSoftwareCount;
        this.leftSoftwareInfo = leftSoftwareInfo;
        this.installTime = installTime;
        this.method = method;
    }
}
