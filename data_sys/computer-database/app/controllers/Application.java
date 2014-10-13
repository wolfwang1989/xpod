package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Computer;
import models.RowInfo;
import models.Stat;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import play.api.libs.json.JacksonJson;
import play.api.mvc.Rendering;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.Tuple4;
import scala.Tuple5;
import views.html.*;

import java.io.IOException;
import java.util.*;

import static play.data.Form.form;

/**
 * Manage a database of computers
 */
public class Application extends Controller {

    /**
     * This result directly redirect to application home.
     */
    public static Result GO_HOME = redirect(
        routes.Application.list(0, "name", "asc", "")
    );
    
    /**
     * Handle default path requests, redirect to computers list
     */
    public static Result index() {
        return GO_HOME;
    }

    /**
     * Display the paginated list of computers.
     *
     * @param page Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order Sort order (either asc or desc)
     * @param filter Filter applied on computer names
     */
    public static Result list(int page, String sortBy, String order, String filter) {
        return ok(
            list.render(
                Computer.page(page, 10, sortBy, order, filter),
                sortBy, order, filter
            )
        );

    }

    public static Result stat(){
        return createStat(null);
    }

    public static Result createStat(Stat count){
        return ok(stat.render(count));
    }



    public static Result createByVersion(String version){
        if(version == null)
            return createStat(null);
        DateTime dateHour = new DateTime();
        Stat count = new Stat(version,new StringBuilder().append(dateHour.getYear()).append("-").append(dateHour.getMonthOfYear()).append("-").append(dateHour.getDayOfMonth())
                .toString());
//        count = new Stat(version,"2014-7-23");
        count.init();
        return createStat(count);
    }

    /**
     * Display the 'edit form' of a existing Computer.
     *
     * @param id Id of the computer to edit
     */
    public static Result edit(Long id) {
        Form<Computer> computerForm = form(Computer.class).fill(
            Computer.find.byId(id)
        );
        return ok(
            editForm.render(id, computerForm)
        );
    }
    
    /**
     * Handle the 'edit form' submission 
     *
     * @param id Id of the computer to edit
     */
    public static Result update(Long id) {
        Form<Computer> computerForm = form(Computer.class).bindFromRequest();
        if(computerForm.hasErrors()) {
            return badRequest(editForm.render(id, computerForm));
        }
        computerForm.get().update(id);
        flash("success", "Computer " + computerForm.get().name + " has been updated");
        return GO_HOME;
    }
    
    /**
     * Display the 'new computer form'.
     */
    public static Result create() {
        Form<Computer> computerForm = form(Computer.class);
        return ok(
            createForm.render(computerForm)
        );
    }
    
    /**
     * Handle the 'new computer form' submission 
     */
    public static Result save() {
        Form<Computer> computerForm = form(Computer.class).bindFromRequest();
        if(computerForm.hasErrors()) {
            return badRequest(createForm.render(computerForm));
        }
        computerForm.get().save();
        flash("success", "Computer " + computerForm.get().name + " has been created");
        return GO_HOME;
    }
    
    /**
     * Handle computer deletion
     */
    public static Result delete(Long id) {
        Computer.find.ref(id).delete();
        flash("success", "Computer has been deleted");
        return GO_HOME;
    }

    public static Result queryUserOperation(String uid,int num ,String uidType,String module){
        try {
            HTableInterface table = Stat.connection.getTable("user_action".getBytes());
            Scan scan = new Scan();
            scan.addColumn("stat".getBytes(), "song_id".getBytes());
            scan.addColumn("stat".getBytes(), "module".getBytes());
            scan.setFilter(new SingleColumnValueFilter("stat".getBytes(),"module".getBytes(), CompareFilter.CompareOp.EQUAL,module.getBytes()));
            scan.setStartRow((uid + "_" + (uidType == null ? "u" : uidType)).getBytes());
            scan.setStopRow((uid + "_" + ( uidType == null ? "u" :uidType ) + 'z').getBytes() );
            ResultScanner scanner = table.getScanner(scan);
            Iterator<org.apache.hadoop.hbase.client.Result> it = scanner.iterator();
            int count = 0;
            List<RowInfo> rows = new ArrayList<RowInfo>();
            while(it.hasNext()){
                org.apache.hadoop.hbase.client.Result row = it.next();
                byte[] songId = row.getValue("stat".getBytes(),"song_id".getBytes());
                if(songId == null)
                    continue;
                RowInfo r = new RowInfo("","",Bytes.toString(songId));
                rows.add(r);
                ++count;
                if(count >= num)
                    break;
            }

            scanner.close();
            table.close();
            return ok(queryUserReal.render(new Tuple5<List<RowInfo>, String, Integer, String,String>(rows,uid,new Integer(num),uidType,module)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final Set<String> INT_KEYS ;
    public static final Long    REDUCER_TIME = 9999999999999L;
    static {
        INT_KEYS = new HashSet<String>();
        INT_KEYS.add("time");
        INT_KEYS.add("net");
        INT_KEYS.add("devtype");
        INT_KEYS.add("tlen");
    }

    /*
        0 < left_time < time < right_time
        REDUCER_TIME - downtime > REDUCER_TIME -time > REDUCER_TIME - uptime
     */
    public static Result getUserActions(String uid,String uidtype,Integer num,String optype,Long left_time,Long right_time ){
        try {
            HTableInterface table = Stat.connection.getTable("user_action".getBytes());
            Scan scan = new Scan();

            if(optype != null)
                scan.setFilter(new SingleColumnValueFilter("stat".getBytes(),"module".getBytes(), CompareFilter.CompareOp.EQUAL, optype.getBytes()));
            String head = (uid + "_" + (uidtype == null ? "u" : uidtype));
            if(right_time != null)
                scan.setStartRow((head + "_" +( REDUCER_TIME - right_time * 1000) ).getBytes());
            else
                scan.setStartRow((head + "_" ).getBytes());

            if(left_time != null){
                scan.setStopRow((head + "_" + (REDUCER_TIME - left_time * 1000)).getBytes());
            }else{
                scan.setStopRow((head + "_" + "z").getBytes());
            }
             ResultScanner scanner = table.getScanner(scan);
            Iterator<org.apache.hadoop.hbase.client.Result> it = scanner.iterator();
            int count = 0;
            List<Map> rows = new ArrayList<Map>();
            Map r = null;
            List<Cell> cells = null;
            org.apache.hadoop.hbase.client.Result row = null;
            String key = null;
            byte[] value = null;
            while(it.hasNext()){
                row = it.next();
                cells = row.listCells();
                r = new HashMap<String,Object>();
                for(Cell c : cells){
                    key = Bytes.toString(CellUtil.cloneQualifier(c));
                    value = CellUtil.cloneValue(c);
                    if(INT_KEYS.contains(key)){
                        if(value.length == 4)
                            r.put(key,Bytes.toInt(value));
                        else
                            r.put(key,Bytes.toLong(value));
                    }else{
                        r.put(key,Bytes.toString(CellUtil.cloneValue(c)));
                    }
                }
                rows.add(r);
                ++count;
                if(count >= num)
                    break;
            }
            scanner.close();
            table.close();
            ObjectMapper mapper = new ObjectMapper();
            return ok(mapper.writeValueAsString(rows));
        } catch (IOException e) {
            e.printStackTrace();
            return ok("IOException:" + e.getMessage());
        }
//        return ok("ok");
    }
}
            
