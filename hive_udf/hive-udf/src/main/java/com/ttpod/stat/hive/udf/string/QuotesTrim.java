package com.ttpod.stat.hive.udf.string;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * 
 * @author Administrator
 * ȥ���ַ���ǰ������˫����
 */
public class QuotesTrim extends UDF{

	
	public String evaluate(String text) {
		if(text==null||"".equals(text.trim())) return null;
		if (text.startsWith("\"")&&text.endsWith("\"")) {
			return text.substring(1, text.length()-1);
		}
		return text;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		QuotesTrim q = new QuotesTrim();
		System.out.println(q.evaluate("\""));
		
		String  f = "[ 4 ]";
		System.out.println(f.length());
	}

}
