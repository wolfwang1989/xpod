import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 14-6-23.
 */
public class DataFactory {
    private static LinkedBlockingQueue<String> data = null;

    static Logger log  = LoggerFactory.getLogger(DataFactory.class);

    static final int DEFAULT_QUESIZE = 10000;
    public static LinkedBlockingQueue<String> getDataQue(){
        return data;
    }

    public static boolean init(int num){
        synchronized (log){
            data = new LinkedBlockingQueue<String>(num);
        }
        return true;
    }

}
