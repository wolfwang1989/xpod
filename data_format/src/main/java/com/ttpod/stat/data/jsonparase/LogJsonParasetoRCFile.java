package com.ttpod.stat.data.jsonparase;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import java.io.IOException;
import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
import org.apache.hadoop.hive.serde2.columnar.BytesRefWritable;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import com.ttpod.stat.fileformat.RCFileOutputFormat;
import org.ttpod.json.JSONArray;
import org.ttpod.json.JSONObject;
import com.ttpod.stat.util.StringtoInteger;

public class LogJsonParasetoRCFile extends Configured implements Tool {
	
//    private static final String SEP = "sep";
    private static final String COLSUM = "colsum"; 
    private static final String CONFIGPATH = "colsum"; 
    private static final StringtoInteger Tool =new StringtoInteger();
	
	public static class LogJsonParasetoRCFileMapper extends Mapper<LongWritable, Text, LongWritable, BytesRefArrayWritable> {
		
		@Override
	    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			
//	        String sep = context.getConfiguration().get("sep");
	        Integer colsum = Tool.atoi(context.getConfiguration().get("colsum"));
//	        String items[] = value.toString().split(sep);
			String items[] = value.toString().split("`");
			String[] lines = new String[colsum];
	        
			Configuration confparams = context.getConfiguration();
	        
	        String[] cparam = confparams.getStrings("param");
	        String[] cdata = confparams.getStrings("data");
	        String cextratime = confparams.get("time");
	        String cextrauuid = confparams.get("uuid");

	        if(items.length == 6){
	        	lines[0] = items[0];
	        	
	        	String jsonStr = items[5];
	            try{
	                JSONObject jsonLog = new JSONObject(jsonStr);
	                JSONArray jsonData = jsonLog.getJSONArray("data");
	                JSONObject jsonParam = jsonLog.getJSONObject("param");
	                
	                String jsonhdtime = (String) jsonLog.get("time");
	                String jsonhduuid = (String) jsonLog.get("uuid");
	                //ttpod_client has uuid separately.
	                lines[1] = jsonhduuid;
	                
	                for(int m = 0; m < cparam.length; m++) {
	                	if(jsonParam.has(cparam[m])){	                		
		                    lines[m+2] = jsonParam.getString(cparam[m]);
		                } else {
		                	lines[m+2] = "";		                	
		                }
	                }
   
	                for(int i = 0; i < jsonData.length(); i++ ){
	                    JSONObject data = jsonData.getJSONObject(i);
	                    for(int n = 0; n < cdata.length; n++) {
		                	if(data.has(cdata[n])){
			                    lines[n+(cparam.length)+2] = data.getString(cdata[n]);
			                } else {
			                	lines[n+(cparam.length)+2] = "";
			                }
		                }
	                    
        	            if (lines.length >= colsum) {
	        	            	byte[][] record = new byte[colsum][colsum];
	        	    	        for (int j = 0; j < colsum; j++) {
	        	    	            record[j] = lines[j].getBytes();
	        	    	        }
	        	                BytesRefArrayWritable bytes = new BytesRefArrayWritable(record.length);

	        	                for (int j = 0; j < record.length; j++) {
	        	                    BytesRefWritable cu = new BytesRefWritable(record[j], 0, record[j].length);
	        	                    bytes.set(j, cu);
	        	                }
	        	                context.write(key, bytes);
	        	            }	                
	                }
	               
	            }catch (Exception e){
	                context.getCounter("Log Parse","Invalid Json").increment(1);
	                return;
	            }
	        }

		}
    }
    
	
	public int run(String[] argv) throws Exception{ 
        Options options = new Options();
//        options.addOption(SEP, true, "eg: 001");
        options.addOption(COLSUM, true, "eg:column sum");
        options.addOption(CONFIGPATH, true, "eg:configuration-params.xml");
        CommandLineParser parser = new GnuParser();
        HelpFormatter helper = new HelpFormatter();
        CommandLine line = null;
        try {
            line = parser.parse(options, argv);
//            if (!(line.hasOption(SEP)
            if (!(line.hasOption(COLSUM)
            		&&line.hasOption(CONFIGPATH))
//                    || "".equals(line.getOptionValue(SEP))
                    || "".equals(line.getOptionValue(COLSUM))
                    || "".equals(line.getOptionValue(CONFIGPATH))) {
                helper.printHelp("DisttibuteOrder", options);
                return 1;
            }
        } catch (Exception e) {
            helper.printHelp("DisttibuteOrder", options);
            e.printStackTrace();
        }

//        String sep = line.getOptionValue(SEP);
        String colsum = line.getOptionValue(COLSUM);
        String configpath = line.getOptionValue(CONFIGPATH);
		
		 Job job = new Job();  
         Configuration conf = job.getConfiguration();
         conf.addResource(configpath);
        
        RCFileOutputFormat.setColumnNumber(conf, Tool.atoi(colsum));
        job.setJarByClass(LogJsonParasetoRCFile.class);

        FileInputFormat.addInputPaths(job, argv[0]);
        FileOutputFormat.setOutputPath(job, new Path(argv[1]));

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(RCFileOutputFormat.class);

        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(BytesRefArrayWritable.class);

        job.setMapperClass(LogJsonParasetoRCFileMapper.class);
        job.setNumReduceTasks(0);


        conf.setBoolean("mapred.output.compress", true);
        conf.set("mapred.output.compression.codec", "org.apache.hadoop.io.compress.SnappyCodec");
        boolean success = (job.waitForCompletion(true));
        return success ? 0 : 1;     
    }

    public static void main(String[] argv) throws Exception{
        int res = ToolRunner.run(new Configuration(), new LogJsonParasetoRCFile(), argv);
        System.exit(res);
    }
}

