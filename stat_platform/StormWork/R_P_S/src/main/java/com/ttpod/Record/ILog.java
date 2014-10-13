package com.ttpod.Record;

import java.util.List;

/**
 * Created by Administrator on 14-6-18.
 */
public interface ILog{
    public ILog getLog(String key);

    public Object getObject(String key);

    public List<Object> getObjectList(String key);

    public List<ILog> getLogList(String key);

    public String getString(String key);
    public List<String> getStringList(String key);

    public Integer getInt(String key);
    public List<Integer> getIntList(String key);
    public Long getLong(String key);
    public List<Long> getLongList(String key);

    public Float getFloat(String key);
    public List<Float> getFloatList(String key);

    public Double getDouble(String key);
    public List<Double> getDoubleList(String key);
}
