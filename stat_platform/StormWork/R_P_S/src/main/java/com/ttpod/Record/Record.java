package com.ttpod.Record;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 * Created by wolf on 14-6-15.
 */
public class Record  implements KryoSerializable,ILog{
    private Map<String,Object> data;
    private static ObjectMapper mapper = new ObjectMapper();
    Record(){super();data = new HashMap<String, Object>();}
    Record(Map da){data = da;}

    static public Record createRecord(String value,List<FieldInfo> fieldInfos) throws Exception {
        Record ret = new Record();
        int start = 0,end = 0;
        String field = null;
        for(int i = 0;i < fieldInfos.size();++i){
            if(i == fieldInfos.size() -1){
                field = value.substring(start);
            }else{
                end = value.indexOf("`",start);
                if(end == -1)
                    return null;
                field = value.substring(start,end);
                start = end + 1;
            }
            FieldInfo fieldInfo = fieldInfos.get(i);
            switch (fieldInfo.type){
                case single:
                    ret.data.put(fieldInfo.fieldName,field);
                    break;
                case list:
                    ret.data.put(fieldInfo.fieldName,field.split(","));
                    break;
                case json:
                    if(!field.startsWith("{")){
                        return null;
                    }
                    ret.data.put(fieldInfo.fieldName,jsonParse(field));
                    break;
                default:
                    throw new Exception("field type not in(single,list,object)");
            }
        }

        return ret;
    }

    private static final Integer def_int = new Integer("0");
    private static final Long def_long = new Long("0");
    private static final Float def_float = new Float(0.0);
    private static final Double def_double =  new Double(0.0);
    private static final String def_string = new String();
    private static final Map def_Map = new TreeMap();
    private static final Object def_Object = new Object();
    public Object getObject(String key){
        return getValue(key,def_Object);
    }

    public List<Object> getObjectList(String key){
        return getListValue(key, def_Object);
    }

    public ILog getLog(String key){
        Map ret =  getValue(key,def_Map);
        return ret == null ? null : new Record(ret);

    }
    public List<ILog> getLogList(String key){
        List<Map> ret =  getListValue(key, def_Map);
        if(ret == null)
            return null;
        List<ILog> lret = new ArrayList<ILog>(ret.size());
        for(Map map : ret){
            lret.add(new Record(map));
        }
        return lret;
    }

    public String getString(String key){
        return getValue(key,def_string);
    }
    public List<String> getStringList(String key){
        return getListValue(key, def_string);
    }

    public Integer getInt(String key){
        return getValue(key,def_int);
    }
    public List<Integer> getIntList(String key){
        return getListValue(key, def_int);
    }
    public Long getLong(String key){
        return getValue(key,def_long);
    }
    public List<Long> getLongList(String key){
        return getListValue(key,def_long);
    }

    public Float getFloat(String key){
        return getValue(key,def_float);
    }
    public List<Float> getFloatList(String key){
        return getListValue(key,def_float);
    }

    public Double getDouble(String key){
        return getValue(key,def_double);
    }
    public List<Double> getDoubleList(String key){
        return getListValue(key,def_double);
    }

    private final  <T> T getValue(String key,T tx){
        Object ret = null;
        int start = 0,end;
        boolean isHead = true;
        while(start < key.length() ){
            end = key.indexOf(".",start);
            end = end == -1 ? key.length():end;
            if(isHead){
                isHead = false;
                ret =(T) data.get(key.substring(start,end));
            }
            else if(ret == null){
                return null;
            }
            else if(ret instanceof Map){
                ret = ((Map) ret).get(key.substring(start,end));
            }
            else if(ret instanceof List && ((List) ret).size() > 0 && ((List) ret).get(0) instanceof Map){
                ret = ((Map) (((List) ret).get(0))).get(key.substring(start,end));
            }
            else{
                return null;
            }
            start = end + 1;
        }
        return (T)ret;
    }
    private final  <T> List<T> getListValue(String key,T tx){
        Object ret = null;
        int start = 0,end;
        boolean isHead = true;
        while(start < key.length() ){
            end = key.indexOf(".",start);
            end = end == -1 ? key.length():end;
            if(isHead){
                isHead = false;
                ret =(T) data.get(key.substring(start,end));
            }
            else if(ret == null){
                return null;
            }
            else if(ret instanceof Map){
                ret = ((Map) ret).get(key.substring(start,end));
            }
            else if(ret instanceof List && ((List) ret).size() > 0 && ((List) ret).get(0) instanceof Map){
                List tmpList = new ArrayList();
                for(Map element : (List<Map>)ret){
                    Object tmp = element.get(key.substring(start,end));
                    if(tmp != null){
                        if(tmp instanceof List){
                            tmpList.addAll((List)tmp);
                        }else{
                            tmpList.add(tmp);
                        }
                    }
                }
                ret  = tmpList.size() == 0 ? null:tmpList;
            }
            else{
                return null;
            }
            start = end + 1;
        }
        if(!(ret instanceof List)){
            List tmp = new ArrayList(1);
            ret = tmp.add(ret);
        }
        return ret == null ? null:(List<T>) ret;
    }

    private  static TreeMap jsonParse(String jsonStr) throws IOException {
        return  mapper.readValue(jsonStr, TreeMap.class);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObject(output,data);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        data = kryo.readObject(input, HashMap.class);
    }
}
