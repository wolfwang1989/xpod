package com.ttpod.stat.data.jsonparase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import java.io.IOException;
import org.ttpod.json.JSONArray;
import org.ttpod.json.JSONObject;

public class LogJsonParasetoTextFile extends Configured implements Tool {
	public static class LogJsonParasetoTextFileMapper extends Mapper<LongWritable, Text, Text,Text> {
		
		@Override
	    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			
			String items[] = value.toString().split("`");
			StringBuilder sb = new StringBuilder();
	        
			Configuration confparams = context.getConfiguration();
	        
	        String[] cparam = confparams.getStrings("param");
	        String[] cdata = confparams.getStrings("data");
	        String cextratime = confparams.get("time");
	        String cextrauuid = confparams.get("uuid");
	        if(items.length == 6) {
	        	sb.append(items[0]);
	        	sb.append('`');
	        	
	        	String jsonStr = items[5];
	            try{	            	
	                JSONObject jsonLog = new JSONObject(jsonStr);
	                JSONArray jsonData = jsonLog.getJSONArray("data");
	                JSONObject jsonParam = jsonLog.getJSONObject("param");
	                
	                String jsonhdtime = (String) jsonLog.get("time");
	                String jsonhduuid = (String) jsonLog.get("uuid");
	                //ttpod_client has uuid separately.
	                sb.append(jsonhduuid);
	                sb.append('`');
	                
	                for(int m = 0; m < cparam.length; m++) {
	                	if(jsonParam.has(cparam[m])){	                		    
		                    sb.append(jsonParam.getString(cparam[m]));
		                } else {
		                	sb.append("");		                	
		                }
	                	sb.append('`');
	                }
   
	                for(int i = 0; i < jsonData.length(); i++ ){
	                    JSONObject data = jsonData.getJSONObject(i);
	                    StringBuilder sbtmp = new StringBuilder();
	                    for(int n = 0; n < cdata.length; n++) {
		                	if(data.has(cdata[n])){
			                    sbtmp.append(data.getString(cdata[n]));
			                } else {
			                	sbtmp.append("");
			                }
		                	sbtmp.append('`');
		                }
	                    sbtmp.deleteCharAt(sbtmp.lastIndexOf("`"));	                    
	        	                context.write(new Text(sb.toString() + sbtmp.toString()), null);
	        	                sbtmp.setLength(0);                
	                }	               
	            }catch (Exception e){
	                context.getCounter("Log Parse","Invalid Json").increment(1);
	                return;
	            }
	        }
		}
    }
    
	
	public int run(String[] argv) throws Exception{
		
		Job job = new Job();  
        Configuration conf = job.getConfiguration();         
        conf.addResource("configuration-params.xml");       
        job.setJarByClass(LogJsonParasetoTextFile.class);
        FileInputFormat.addInputPaths(job, argv[0]);
        FileOutputFormat.setOutputPath(job, new Path(argv[1]));
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        job.setMapperClass(LogJsonParasetoTextFileMapper.class);
        job.setNumReduceTasks(0);

        conf.setBoolean("mapred.output.compress", true);
        conf.set("mapred.output.compression.codec", "org.apache.hadoop.io.compress.SnappyCodec");
        boolean success = (job.waitForCompletion(true));
        return success ? 0 : 1;     
    }

    public static void main(String[] argv) throws Exception{
        int res = ToolRunner.run(new Configuration(), new LogJsonParasetoTextFile(), argv);
        System.exit(res);
    }
}

