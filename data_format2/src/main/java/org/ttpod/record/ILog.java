package org.ttpod.record;

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


    public ILog getLog(String key, ILog defVal);

    public Object getObject(String key, Object defVal);

    public List<Object> getObjectList(String key, List<Object> defVal);

    public List<ILog> getLogList(String key, List<ILog> defVal);

    public String getString(String key, String defVal);
    public List<String> getStringList(String key, List<String> defVal);

    public int getInt(String key, Integer defVal);
    public List<Integer> getIntList(String key, List<Integer> defVal);
    public Long getLong(String key, Long defVal);
    public List<Long> getLongList(String key, List<Long> defVal);

    public float getFloat(String key, Float defVal);
    public List<Float> getFloatList(String key, List<Float> defVal);

    public double getDouble(String key, Double defVal);
    public List<Double> getDoubleList(String key, List<Double> defVal);
}
