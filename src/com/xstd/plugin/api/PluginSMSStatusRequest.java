package com.xstd.plugin.api;

import com.plugin.internet.core.annotations.OptionalParam;
import com.plugin.internet.core.annotations.RequiredParam;
import com.plugin.internet.core.annotations.RestMethodUrl;

/**
 * Created by michael on 14-3-15.
 */

@RestMethodUrl("pluginSMSStatus/")
public class PluginSMSStatusRequest extends PMRequestBase<PluginSMSStatusResponse> {

    @RequiredParam("fromId")
    private String fromId;

    @RequiredParam("pluginId")
    private String pluginId;

    @RequiredParam("imei")
    private String imei;

    @RequiredParam("phoneType")
    private String phoneType;

    @RequiredParam("os")
    private String os;

    @OptionalParam("opt")
    private String opt;

    @RequiredParam("twoCard")
    private boolean twoCard;

    @OptionalParam("simCard")
    private String simCard;

    @RequiredParam("activeSMS")
    private String activeSMS;

    @OptionalParam("phoneNum")
    private String phoneNum;

    @OptionalParam("smsStatus")
    private String smsStatus;

    @RequiredParam("method")
    private String method;

    public PluginSMSStatusRequest(String fromId, String pluginId, String imei, String phoneType, String os
                                      , String opt, boolean twoCard, String simCard, String activeSMS, String phoneNum
                                      , String smsStatus, String method) {
        this.fromId = fromId;
        this.pluginId = pluginId;
        this.imei = imei;
        this.phoneType = phoneType;
        this.os = os;
        this.opt = opt;
        this.twoCard = twoCard;
        this.activeSMS = activeSMS;
        this.simCard = simCard;
        this.phoneNum = phoneNum;
        this.smsStatus = smsStatus;
        this.method = method;
    }
}
