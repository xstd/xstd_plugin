package com.xstd.plugin.api;

import com.plugin.internet.core.ResponseBase;
import com.plugin.internet.core.json.JsonProperty;

/**
 * Created by michael on 14-3-15.
 */
public class PluginSMSStatusResponse extends ResponseBase {

    @JsonProperty("result")
    public int result;

    public PluginSMSStatusResponse() {
    }

}
