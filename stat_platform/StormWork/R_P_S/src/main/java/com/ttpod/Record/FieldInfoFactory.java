package com.ttpod.Record;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-6-18.
 */
public class FieldInfoFactory {
   static Logger log = LoggerFactory.getLogger(FieldInfoFactory.class);
   public  static List<FieldInfo> getFieldInfo(String input){
        if(input == null){
            log.error("input is Null");
            return null;
        }
        List<FieldInfo> lret = new ArrayList<FieldInfo>();
        String[] fieldStrs = input.split(",");
        for(String field : fieldStrs){
            String[] info = field.split(":");
            if(info.length != 2){
                log.error("FieldInfo create failed,length != 2. config:" + input);
                return null;
            }
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.fieldName = info[0];
            if (info[1].equals("single")) {
                fieldInfo.type = FieldType.single;
            }
            else if(info[1].equals("list")){
                fieldInfo.type = FieldType.list;
            }
            else if(info[1].equals("json")){
                fieldInfo.type = FieldType.json;
            }
            else{
                log.error("field type must be in [single,list,json],config:" + input);
                return null;
            }
            lret.add(fieldInfo);
        }
        return lret;
    }

}
