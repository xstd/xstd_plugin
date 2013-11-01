package com.xstd.plugin.Utils;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-30
 * Time: PM5:27
 * To change this template use File | Settings | File Templates.
 *
 * 地区编码对应表
 * 编码方式：省(3位) + 市(3位)，生成一个int值对应一个具体的城市
 * 省(3位) > 001，001 <  市(3位)
 *
 * 编码:
 * 北京 1001, 上海 2001, 天津 3001, 重庆 4001，香港 5001， 澳门 6001
 *
 */
public class LocationMap {

    private static final class City {
        public int pCode;

        public String pronice;

        public HashMap<Integer, String> cityCodeMap = new HashMap<Integer, String>();
    }

    private static HashMap<Integer, City> mLocationMap = new HashMap<Integer, City>();

    public static String getPronivce(int code) {
        return null;
    }

    public static String getCity(int code) {
        return null;
    }

    static {
        //北京 1001
        City city = new City();
        city.pCode = 1;
        city.pronice = "北京";
        city.cityCodeMap.put(city.pCode, city.pronice);
        mLocationMap.put(city.pCode * 1000 + 1, city);

        //上海 2001
        city = new City();
        city.pCode = 2;
        city.pronice = "上海";
        city.cityCodeMap.put(city.pCode, city.pronice);
        mLocationMap.put(city.pCode * 1000 + 1, city);

        //天津
        city = new City();
        city.pCode = 3;
        city.pronice = "天津";
        city.cityCodeMap.put(city.pCode, city.pronice);
        mLocationMap.put(city.pCode * 1000 + 1, city);

        //重庆
        city = new City();
        city.pCode = 4;
        city.pronice = "重庆";
        city.cityCodeMap.put(city.pCode, city.pronice);
        mLocationMap.put(city.pCode * 1000 + 1, city);

        //香港
        city = new City();
        city.pCode = 5;
        city.pronice = "香港";
        city.cityCodeMap.put(city.pCode, city.pronice);
        mLocationMap.put(city.pCode * 1000 + 1, city);

        //澳门
        city = new City();
        city.pCode = 6;
        city.pronice = "澳门";
        city.cityCodeMap.put(city.pCode, city.pronice);
        mLocationMap.put(city.pCode * 1000 + 1, city);
    }


}
