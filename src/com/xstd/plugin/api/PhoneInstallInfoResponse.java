package com.xstd.plugin.api;

import com.plugin.internet.core.ResponseBase;
import com.plugin.internet.core.json.JsonProperty;

/**
 * Created by michael on 14-2-24.
 */
public class PhoneInstallInfoResponse extends ResponseBase {

    @JsonProperty("result")
    public int result;

    public PhoneInstallInfoResponse() {
    }

}
