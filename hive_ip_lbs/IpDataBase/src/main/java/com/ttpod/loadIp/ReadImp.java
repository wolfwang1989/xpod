package com.ttpod.loadIp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;

/**
 * Created by wolf on 14-5-11.
 */
public class ReadImp implements ReadInterface {
    static Log log = LogFactory.getLog(ReadImp.class);
    String fileName ;
    FileReader inputFile;
    StringBuilder courrent;
    Config conf;
    static final long one = 1;
    @Override
    public boolean init(Config conf) {
        fileName = conf.inputFileName;
        this.conf = conf;

        courrent = new StringBuilder();
        try {
            inputFile = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }
    void format(Location loc){
        String newCountry = "";
        String newProvince = "";
        String newCity = "";
        if(loc.country != null ){
            Matcher matcher = Constant.PA_CHINA_PROVINCE.matcher(loc.country);
            if(matcher.find()){
                newCountry = "中国";
                newProvince = matcher.group() + "省";
                loc.area = Constant.PRO_TO_AREA.get(matcher.group().trim());
            }else{
                matcher = Constant.PA_CHINA_CITY.matcher(loc.country);
                if(matcher.find()){
                    newCountry = "中国";
                    newCity = matcher.group().trim();
                }
            }

        }
        if(loc.province != null){
            Matcher matcher = Constant.PA_CHINA_PROVINCE.matcher(loc.province);
            if(matcher.find()){
                newCountry = "中国";
                newProvince = matcher.group() + "省";
                loc.area = Constant.PRO_TO_AREA.get(matcher.group()).trim();
            }else{
                matcher = Constant.PA_CHINA_CITY.matcher(loc.province);
                if(matcher.find()){
                    newCountry = "中国";
                    newCity = matcher.group().trim();
                }
            }

        }
        if(loc.city != null){
            Matcher matcher = Constant.PA_CHINA_CITY.matcher(loc.city);
            if(matcher.find()){
                newCountry = "中国";
                newCity = matcher.group().trim();
            }
        }
        if(loc.county != null){
            Matcher matcher = Constant.PA_CHINA_CITY.matcher(loc.county);
            if(matcher.find()){
                newCountry = "中国";
                newCity = matcher.group().trim();
            }
        }
        if(newCountry.length() > 0){
            loc.country = newCountry;
            loc.province = newProvince;
            loc.city = newCity;
        }
    }
    @Override
    public Location read() {
        
        Location ret = new Location();
        try{
        	JSONArray array = new JSONArray(courrent.toString());
            String ips = (String)array.get(0);
	        {
	           String[] ipPair =  ips.split("/");
	           ret.beginIp = ipPair[0];
               ret.nBeginIp = Util.ipToLong(ret.beginIp);
               if(conf.isSubNet){
//                    ret.nEndIp = ret.nBeginIp + (one <<(32- Long.parseLong(ipPair[1]))) - 1;
                    ret.nEndIp = Util.ipToLong(ipPair[1]);
               }
               else
                    ret.nEndIp = ret.nBeginIp + Long.parseLong(ipPair[1]);
               ret.endIp = Util.longToIP(ret.nEndIp);
	        }
	        {
	            JSONObject addr = array.getJSONObject(1).getJSONObject("address");
	            if(addr.has("area"))
	            	ret.area = addr.getString("area").trim();
	            if(addr.has("city"))
	            	ret.city = addr.getString("city").trim();
	            if(addr.has("country"))
	            	ret.country = addr.getString("country").trim();
	            if(addr.has("isp"))
	            	ret.isp = addr.getString("isp").trim();
                if(addr.has("province"))
                    ret.province = addr.getString("province").trim();
                if(addr.has("county"))
                    ret.county = addr.getString("county").trim();
                if(addr.has("other"))
	            	ret.other = addr.getString("other").trim();
	        }
            format(ret);

        }catch(JSONException e){
        	log.error(e.getMessage());
        	return null;
        }
        return  ret;
    }
    boolean eof = false;
    @Override
    public boolean hasNext() {
        try {
            if(eof)
                return false;
            courrent.delete(0,courrent.capacity());
            int c;
            while((c =  inputFile.read()) != -1 ){

                if(c == '\n'){
                    break;
                }else{
                    courrent.append((char)c);
                }
            }
            if(c == -1)
                eof = true;

        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
        return courrent.capacity() > 0 ? true : false;
    }

    @Override
    public void close() throws IOException {
        inputFile.close();
    }

}
