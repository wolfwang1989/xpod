package com.ttpod.stat.util;

public class StringtoInteger {
	private final static int INT_MAX = 2147483647;
    private final static int INT_MIN = -2147483648;
    
    public int atoi(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        char[] ch = str.trim().toCharArray();
        if (ch.length == 0) {
            return 0;
        }
        int head = 0;
        long value = 0, signal = 1;
        if (ch[head] == '+' || ch[head] == '-') {
            signal = ch[head++] == '+' ? 1 : -1;
        }
        while (head < ch.length && ch[head] >= '0' && ch[head] <= '9') {
            value = value * 10 + (ch[head++] - '0');
            if (value * signal < INT_MIN || value * signal > INT_MAX) {
                return value *signal < INT_MIN ? INT_MIN : INT_MAX;
            }
        }
        return (int) (value * signal);
    }

}
