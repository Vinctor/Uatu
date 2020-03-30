package com.vinctor.uatu.manager;

import android.util.Log;

import com.vinctor.uatu.room.ThreadPool;
import com.vinctor.uatu.room.TraceRecord;
import com.vinctor.uatu.room.TraceRecordDao;
import com.vinctor.uatu.room.UatuDatabase;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UatuManager {

    static UatuDatabase instance = UatuDatabase.getInstance();
    static ConcurrentHashMap<String, Long> tsMap = new ConcurrentHashMap();
    static ConcurrentHashMap<String, String> argmentMap = new ConcurrentHashMap();

    public static String start(final String className, final String method, final String signature, Object[] args) {
        long startTs = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        //Log.d("IUatuInterface-start", className + "--" + method);
        String tsKey = getTsKey(className, method, signature, id);
        tsMap.put(tsKey, startTs);
        argmentMap.put(tsKey, parseArgs(args));
        return id;
    }


    public static void end(String id, final String className, final String method, final String signature, final Object returnObj) {
        final long endTs = System.currentTimeMillis();
        Log.d("IUatuInterface-end", className + "--" + method + "--" + (returnObj == null ? "null" : returnObj.toString()));
        String tsKey = getTsKey(className, method, signature, id);
        Long startTsMap = tsMap.get(tsKey);
        tsMap.remove(tsKey);
        final String args = argmentMap.get(tsKey);
        argmentMap.remove(tsKey);
        final long startTs = startTsMap == null ? 0 : startTsMap;

        if (instance == null) {
            instance = UatuDatabase.getInstance();
        }
        if (instance == null) {
            return;
        }
        final TraceRecordDao traceRecoedDao = instance.getTraceRecoedDao();

        final String threadName = Thread.currentThread().getName();
        ThreadPool.getSqlThreadPool().execTask(new Runnable() {
            @Override
            public void run() {
                TraceRecord record = new TraceRecord();
                record.className = className;
                record.methodName = method;
                record.signature = signature;
                record.threadName = threadName;
                record.startTs = startTs;
                record.endTs = endTs;
                record.argments = args;
                record.return_data = (returnObj == null ? "null" : returnObj.toString());
                traceRecoedDao.insertRecord(record);
            }
        });
    }

    public static String parseArgs(Object[] args) {
        int length = args.length;
        if (length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            Object current = args[i];
            String currentString;
            if (current == null) {
                currentString = "[null]";
            } else {
                currentString = current.toString();
            }
            builder.append(currentString);
            if (i != length - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    private static String getTsKey(String className, String method, String signature, String id) {
        return new StringBuilder()
                .append(className)
                .append("-")
                .append(method)
                .append("-")
                .append(signature)
                .append("-")
                .append(id)
                .toString();
    }
}
