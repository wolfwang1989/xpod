import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 14-6-23.
 */
public class DataReader implements Runnable{
    static Logger log = LoggerFactory.getLogger(DataFactory.class);
    String cmd;
    LinkedBlockingQueue<String> queue;
    public  DataReader(String command){
        this.cmd = command;
        queue = DataFactory.getDataQue();
        new Thread(this).start();
    }


    @Override
    public void run() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            log.error("excuete cmd failed:" + cmd, e);
            System.exit(0);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(process
                .getInputStream()));
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                queue.put(line);
            }
        } catch (IOException e) {
            log.error("",e);
        } catch (InterruptedException e) {
            log.error("put element error", e);
            System.exit(0);
        }


        try {
            process.waitFor();
            br.close();
        } catch (IOException e) {
            log.error("",e);
        } catch (InterruptedException e) {
            log.error("",e);
        }
        System.exit(0);
    }
}
