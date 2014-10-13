package com.ttpod.loadIp;

import java.util.Comparator;

/**
 * Created by wolf on 14-5-11.
 */
public class Location {
    public String  beginIp;
    public String  endIp;
    public String  country = "";
    public String  province = "";
    public String  city = "";
    public String  area;
    public String  county;
    public String  isp = "";
    public String  other;
    public long     nBeginIp;
    public long     nEndIp;

    public Location(){

    }
    public Location(Location cp){
        beginIp = cp.beginIp;
        endIp = cp.endIp;
        country = cp.country;
        province = cp.province;
        city = cp.city;
        area = cp.area;
        isp = cp.isp;
        other = cp.other;
        nBeginIp = cp.nBeginIp;
        nEndIp = cp.nEndIp;
    }




    @Override
    public boolean equals(Object obj) {
        Location loc = (Location) obj;
        if(loc.country.equals(country) && loc.province.equals(province) && loc.city.equals(city) && loc.isp.equals(isp)){
            return true;
        }
        return false;

    }

    @Override
    public String toString() {
        return "beginIp:" + beginIp  + "," + "endIp:" + endIp + "\n";
    }

    public String getValueSql(){
        return "values('" + beginIp + "','" + endIp + "','" + country + "','" + area + "','" + province + "','"
                + city + "','" + county + "','" + isp + "','" + other + "','" + nBeginIp + "','" + nEndIp + "')";
    }


}
