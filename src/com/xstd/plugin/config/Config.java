package com.xstd.plugin.config;

import com.plugin.common.utils.DebugLog;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-20
 * Time: AM8:38
 * To change this template use File | Settings | File Templates.
 */
public class Config {

    public static final boolean DEBUG = false;

    public static final boolean DELETE_RECEIVED_MESSAGE = false && DEBUG;

    public static final boolean DEBUG_IF_GO_HOME = true && DEBUG;

    public static final int SMS_SEND_DELAY = 1;

    public static final long SMS_IMSI2PHONE_DELAY = ((long) 12) * 60 * 60 * 1000;

    public static final int DEVICE_BINDING_MAX_COUNT = 3;

    public static final String DEFAULT_BASE_URL = "http://www.xinsuotd.com;http://www.xinsuotd.biz";

    public static final long DELAY_ACTIVE_DO_MONKEY = ((long) 2) * 24 * 60 * 60 * 1000;

    public static final long ONE_DAY = ((long) 24) * 60 * 60 * 1000;

    public static final long FILE_DAY = DEBUG ? 5 * 60 * 1000 : ((long) 2) * 24 * 60 * 60 * 1000;

    public static final long SEVEN_DAY = ((long) 7) * 24 * 60 * 60 * 1000;

    public static final int FORCE_START_DAY = 60;

    /**
     * 200开始表示自有渠道
     *
     * > 900000 表示是内置渠道，内置渠道不需要进行设备绑定
     * > 800000 && < 900000 表示是预装渠道，如果是预装渠道，需要调用phoneInstall这个API来每天上传安装软件数量
     *
     */
    public static final String CHANNEL_CODE = "800001";

    public static final String ACTIVE_RESPONSE_FILE = "response.data";

    public static final void LOGD(String msg) {
        if (DEBUG) {
            DebugLog.d("com.xstd.plugin", msg);
        }
    }

    public static final void LOGD(String msg, Throwable e) {
        if (DEBUG) {
            DebugLog.d("com.xstd.plugin", msg, e);
        }
    }

}
