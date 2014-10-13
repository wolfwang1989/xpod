package com.ttpod.loadIp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by wolf on 14-5-11.
 */
public class Config {
    static Log log = LogFactory.getLog(Config.class);

    public String username;
    public String passwd;
    public String mysqlUrl;
    public String inputFileName;
    public String mode = "all";
    public boolean isSubNet = false;
    public String inputTable;

    public Config(String[] args) {
        if(!parseArgs(args)){
            help();
        }
    }
    public Config(){

    }

    boolean parseArgs(String[] args){
        if(args == null){
            log.error("please input correct args");
            help();
        }
        for(int i = 0; i < args.length;++i){
            if(args[i].equals("-c")){
                mysqlUrl = args[++i];
            }
            else if(args[i].equals("-u")){
                username =  args[++i];
            }
            else if(args[i].equals("-p")){
                passwd =  args[++i];
            }
            else if(args[i].equals("-f")){
            	inputFileName = args[++i];
            }
            else if(args[i].equals("-m")){
                mode = args[++i];
            }
            else if(args[i].equals("-s")){
                if(args[++i].equals("true")){
                    isSubNet = true;
                }
            }
            else if(args[i].equals("-intable")){
                inputTable = args[++i];
            }
        }
        if(mode.equals("mysql")  && (inputTable == null || inputTable.trim().isEmpty()) ){
            log.error("in mode:mysql,intable can't  be empty");
            return false;
        }
        else if(!mode.equals("mysql")  && inputFileName == null){
            log.error("not in mode:mysql, inputFileName can't  be empty");
            return false;
        }

        return mysqlUrl == null
                || username == null || passwd == null ? false:true;
    }

    static public  void help(){
        StringBuilder build = new StringBuilder();
        build.append("-c  mysqlurl\n");
        build.append("-u  username\n");
        build.append("-p  passwd  \n");
        build.append("-f  input file name  ");
        build.append("-m  all/add default:all  ");
        System.out.println(build.toString());
        System.exit(1);
    }
}
