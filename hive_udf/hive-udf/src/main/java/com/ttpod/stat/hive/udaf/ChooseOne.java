package com.ttpod.stat.hive.udaf;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hive.ql.exec.UDAF;
import org.apache.hadoop.hive.ql.exec.UDAFEvaluator;

public class ChooseOne extends UDAF {
	public static class State {
		// private long mCount;
		// private double mSum;
		private Map<String, Long> mapCount;
		private String type = "mid";
	}

	public static class ChooseOneEvaluator implements UDAFEvaluator {
		State state;

		public ChooseOneEvaluator() {
			super();
			state = new State();
			init();
		}

		/** * init���������ڹ��캯������UDAF�ĳ�ʼ�� */
		public void init() {
			state.mapCount = new HashMap<String, Long>();
		}

		/** * iterate���մ���Ĳ�������ڲ�����ת���䷵������Ϊboolean * * @param o * @return */
		public boolean iterate(String key, String type) {
			
			if (key != null && !(key.trim().equals(""))) {
				if ("tid".equals(type) && "0".equals(key))return true;
				Long v = state.mapCount.get(key);
				state.mapCount.put(key, v == null ? 1 : v + 1);
				state.type = type;
			}
			return true;
		}

		/**
		 * * terminatePartial�޲�����Ϊiterate������ת����󣬷�����ת��ݣ� *
		 * terminatePartial������hadoop��Combiner * * @return
		 */
		public State terminatePartial() {
			// combiner
			return state.mapCount.size() == 0 ? null : state;
		}

		/**
		 * * merge����terminatePartial�ķ��ؽ��������merge�������䷵������Ϊboolean * * @param o
		 * * @return
		 */
		public boolean merge(State sta) {
			if (sta != null && sta.mapCount.size() > 0) {
				for (String e : sta.mapCount.keySet()) {
					Long l = state.mapCount.get(e);
					state.mapCount.put(e, l == null ? sta.mapCount.get(e) : l
							+ sta.mapCount.get(e));
				}

			}
			return true;
		}

		/** * terminate�������յľۼ������� * * @return */
		public String terminate() {
			if (state.mapCount.keySet().isEmpty()) {
				return null;
			}
			return state.mapCount.keySet().iterator().next();
		}
	}

	public static void main(String[] args) {
		ChooseOneEvaluator f = new ChooseOne.ChooseOneEvaluator();
        f.init();
		f.iterate("10000000000000000000000000000000000", "tid");
		//f.iterate("0", "tid");
		//f.iterate("0", "tid");
		//f.iterate("0", "tid");
		f.merge(f.state);
		System.out.println(f.terminate());
		// new ChooseOne.ChooseOneEvaluator().merge("test");
	}
}