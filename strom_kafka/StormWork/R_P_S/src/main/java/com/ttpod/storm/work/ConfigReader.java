package com.ttpod.storm.work;

import backtype.storm.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.ArrayUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-6-17.
 */
public class ConfigReader {
    public static final String STREAM_CONF_KEY = "com.ttpod.componentinfo";

    static private Logger log = org.slf4j.LoggerFactory.getLogger(ConfigReader.class);

    static boolean buildComponentInfo(String text,Config conf) throws IOException {

        ComponentInfo comInfo = new ObjectMapper().readValue(text, ComponentInfo.class);


        if(comInfo != null){
            if(!comInfo.buildIndex()){
                return false;
            }
            conf.put(STREAM_CONF_KEY,comInfo);
            return true;
        }
        else{
            log.error("parse json to componetInfo failed");
        }
        return false;
    }
    public static Config createStormConfig(String filename) throws DocumentException {

        SAXReader read = new SAXReader();

        Document doc = read.read(new File(filename));

        Element root = doc.getRootElement();
        @SuppressWarnings("unchecked")
        List<Element> childs = root.elements();
        Config config = new Config();

        for(Element n :childs){
            Element name = n.element("name");
            Element value = n.element("value");
            if(name.getText().equals("stream")){
                try {

                    if(!buildComponentInfo(value.getText(), config))
                        return null;

                } catch (Exception e) {
                    log.error("create stream config failed",e);

                    System.exit(0);
                    return null;
                }
                continue;
            }
            Element type = n.element("type");
            if(type == null || type.getTextTrim().equals("string")){
                config.put(name.getTextTrim(), value.getTextTrim());
            }
            else if(type.getTextTrim().equals("bool")){
                if(value.getText().equals("true"))
                    config.put(name.getTextTrim(), true);
                else if(value.getText().equals("false")){
                    config.put(name.getTextTrim(), false);
                }
            }
            else if(type.getTextTrim().equals("int")){
                config.put(name.getTextTrim(), Integer.parseInt(value.getTextTrim()));
            }
            else if(type.getTextTrim().equals("list")){
                config.put(name.getTextTrim(), new ArrayList<String>(java.util.Arrays.asList(value.getTextTrim().split(","))));
            }
            else if(type.getTextTrim().equals("file")){
                try {

                    java.io.InputStream inputStream = new FileInputStream(value.getTextTrim());

                    List<Integer> buf = new ArrayList<Integer>();
                    int a = inputStream.read();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    while(a != -1){
                        buf.add(a);
                        a = inputStream.read();
                    }
                    inputStream.close();
                    config.put(name.getTextTrim(),buf);
                } catch (FileNotFoundException e) {
                    log.error("file not found:" + value.getTextTrim() ,e);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return config;

    }

    public static void main(String[] agrs) throws DocumentException {
        Config config = createStormConfig("./dbusSpout.xml");

    }

}
