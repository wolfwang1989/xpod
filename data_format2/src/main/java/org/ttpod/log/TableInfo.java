package org.ttpod.log;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.ttpod.record.ILog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 14-9-4.
 */
public class TableInfo {
    static Logger  log = Logger.getLogger(TableInfo.class);

    public static List<Field> getTableInfo(String info, Configuration conf){
        String[] fields = info.split(",");
        if(fields.length != conf.getInt("hive.io.rcfile.column.number.conf" , -1)){
            log.error("com.ttpod.log.hive.table.info length  !=  hive.io.rcfile.column.number.conf ");
            return null;
        }
        List<Field> ret = new ArrayList<Field>(conf.getInt("hive.io.rcfile.column.number.conf", -1));
        for(String field : fields){
            Field t = Field.createField(field);
            if(t == null){
                return null;
            }
            ret.add(t);
        }
        return ret;
    }

    public enum FieldType{
        INT,
        LONG,
        DOUBLE,
        STRING;
        static Map<String,FieldType> NAME_TO_TYPE ;
        static {
            NAME_TO_TYPE = new HashMap<String, FieldType>();
            NAME_TO_TYPE.put("int",INT);
            NAME_TO_TYPE.put("long",LONG);
            NAME_TO_TYPE.put("double",DOUBLE);
            NAME_TO_TYPE.put("string",STRING);
        }

        public static FieldType getType(String type){
            return NAME_TO_TYPE.get(type);
        }
    }
    public static class Field{
        String name;
        FieldType type;
        boolean isOperation;
        public Field(String key,FieldType fieldType){
            name = key;
            type = fieldType;
            isOperation = name.startsWith("$.");
            if(isOperation)
                name = name.replace("$.","");
        }


        public  byte[] getValue(ILog operation,ILog pubinfo){
            ILog op = isOperation ? operation : pubinfo;
            switch (type){
                case INT:
                {
                        Object val = op.getObject(name);
                        if(val instanceof Integer){
                            return Bytes.toBytes((Integer)val);
                        }
                        else if(val instanceof Long){
                            return Bytes.toBytes(((Long) val).intValue());
                        }
                        else if(val instanceof Double){
                            return Bytes.toBytes(((Double) val).intValue());
                        }else if(val instanceof String){
                            return Bytes.toBytes(Integer.parseInt((String)val));
                        }
                        return null;
                }
                case DOUBLE:
                {
                    Object val = op.getObject(name);
                    if(val instanceof Integer){
                        return Bytes.toBytes(new Double((Integer)val));
                    }
                    else if(val instanceof Long){
                        return Bytes.toBytes((new Double((Long)val)));
                    }
                    else if(val instanceof Double){
                        return Bytes.toBytes((Double) val);
                    }else if(val instanceof String){
                        return Bytes.toBytes(Double.parseDouble((String) val));
                    }
                    return null;
                }
                case LONG:
                {
                    Object val = op.getObject(name);
                    if(val instanceof Integer){
                        return Bytes.toBytes((Integer)val);
                    }
                    else if(val instanceof Long){
                        return Bytes.toBytes(((Long) val).intValue());
                    }
                    else if(val instanceof Double){
                        return Bytes.toBytes(((Double) val).longValue());
                    }else if(val instanceof String){
                        return Bytes.toBytes(Long.parseLong((String) val));
                    }
                    return null;
                }
                case STRING:
                {
                    Object val = op.getObject(name);
                    if(val instanceof Integer){
                        return Bytes.toBytes(((Integer)val).toString());
                    }
                    else if(val instanceof Long){
                        return Bytes.toBytes(((Long) val).toString());
                    }
                    else if(val instanceof Double){
                        return Bytes.toBytes(((Double) val).toString());
                    }else if(val instanceof String){
                        return Bytes.toBytes((String)val);
                    }
                    return null;
                }
            }
            return null;

        }



        public static  Field createField(String field){
            String[] infos = field.split(":");
            if(infos.length != 2){
                log.error("field length != 2 :" + field);
                return  null;
            }
            FieldType fieldType = FieldType.getType(infos[1]);
            if(fieldType == null){
                log.error("field type  not in(int,long,double,string) :" + field);
                return null;
            }
            return new Field(infos[0],fieldType);
        }
    }
}
