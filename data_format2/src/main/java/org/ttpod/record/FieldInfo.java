package org.ttpod.record;

/**
 * Created by wolf on 14-6-15.
 */
public  class FieldInfo{
    FieldType type;
    String fieldName;
    public FieldInfo setType(FieldType fieldType){
        type = fieldType;
        return this;
    }
    public FieldInfo setFieldName(String name){
        fieldName = name;
        return this;
    }
}
