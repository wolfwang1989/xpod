import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Administrator on 14-6-23.
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        String fileName = args[0];
        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(fileName);
        properties.load(inputStream);
        inputStream.close();
        String topicName = properties.getProperty("topicname");
        int size = Integer.parseInt(properties.getProperty("batch.size"));
        int quesize = Integer.parseInt(properties.getProperty("queue.size"));
        DataFactory.init(quesize);
        LogProducer producer = new LogProducer(properties,topicName,size);
        DataReader  reader = new DataReader(properties.getProperty("command.line"));
        while(true){
            Thread.sleep(1000000);
        }
    }
}
