import com.ttpod.Record.FieldInfo;
import com.ttpod.Record.FieldType;
import com.ttpod.Record.Record;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-6-25.
 */
public class RecordTest {

    static void testRecord() throws Exception {
        List<FieldInfo> fieldInfoList = new ArrayList<FieldInfo>();
        fieldInfoList.add(new FieldInfo().setFieldName("a").setType(FieldType.single));
        fieldInfoList.add(new FieldInfo().setFieldName("b").setType(FieldType.single));
        fieldInfoList.add(new FieldInfo().setFieldName("c").setType(FieldType.single));
        fieldInfoList.add(new FieldInfo().setFieldName("d").setType(FieldType.single));
        fieldInfoList.add(new FieldInfo().setFieldName("e").setType(FieldType.single));
        fieldInfoList.add(new FieldInfo().setFieldName("f").setType(FieldType.json));
        Assert.assertTrue(Record.createRecord("a`b`c`d`e`f", fieldInfoList) == null);
        Assert.assertTrue(Record.createRecord("a`b`c`d`e`-", fieldInfoList) == null);
        Assert.assertTrue(Record.createRecord("a`b`c`d`e`{}", fieldInfoList) != null);
        Assert.assertTrue(Record.createRecord("a`b`c`d`e`{\"`\":\"data\"}", fieldInfoList) != null);
    }
    public static void main(String[] args) throws Exception {
        testRecord();
        return;
    }
}
