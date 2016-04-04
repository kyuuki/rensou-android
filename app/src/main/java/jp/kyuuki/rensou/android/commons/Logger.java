package jp.kyuuki.rensou.android.commons;

import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.util.Log;

public class Logger {
    private static final String TAG = Logger.class.getName();

    private static final int MAX_MESSAGE = 1000;

    // Class

    private static Logger instance;

    static {
        Log.v(TAG, "Logger initialize.");
        instance = new Logger();
        instance.messages.add("Logger start. [" + new Date() + "]");
    }

    public static int e(String tag, String msg) {
        instance.add(msg);
        return Log.e(tag, msg);
    }
    public static int w(String tag, String msg) {
        instance.add(msg);
        return Log.w(tag, msg);
    }
    public static int i(String tag, String msg) {
        instance.add(msg);
        return Log.i(tag, msg);
    }
    public static int d(String tag, String msg) {
        instance.add(msg);
        return Log.d(tag, msg);
    }
    public static int v(String tag, String msg) {
        instance.add(msg);
        return Log.v(tag, msg);
    }

    public static String getMessages() {
        StringBuffer buffer = new StringBuffer();
        Iterator<String> ite = instance.messages.iterator();
        while (ite.hasNext()) {
            buffer.append(ite.next()).append("\n");
        }
        return buffer.toString();
    }

    // Object

    private Queue<String> messages;

    private Logger() {
        this.messages = new ConcurrentLinkedQueue<String>();
    }

    private void add(String msg) {
        if (messages.size() > MAX_MESSAGE) {
            messages.poll();
        }
        messages.offer(msg);
    }
}
