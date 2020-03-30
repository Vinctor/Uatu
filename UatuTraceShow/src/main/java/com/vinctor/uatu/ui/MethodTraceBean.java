package com.vinctor.uatu.ui;

import com.vinctor.uatu.room.TraceRecord;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MethodTraceBean {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    String methodName;
    String className;
    String desc;
    String startTime;
    String endTime;
    Long cost;
    String threadName;
    String argments;

    public MethodTraceBean(TraceRecord record) {
        methodName = record.methodName;
        className = record.className.replaceAll("/", "\\.");
        startTime = parseTs(record.startTs);
        endTime = parseTs(record.endTs);
        cost = record.endTs - record.startTs;
        threadName = record.threadName;
        desc = parseSignature(record.signature);
        argments = record.argments;
    }

    private String parseTs(Long ts) {
        Date date = new Date(ts);
        String formatString = sdf.format(date);
        return formatString;
    }

    private String parseSignature(String signature) {
        StringBuilder sb = new StringBuilder();
        String[] splitSignature = signature.split(",");
        int length = splitSignature.length;
        for (int i = 0; i < length; i++) {
            String item = splitSignature[i];
            if (item.contains(".")) {
                String[] splitClassName = item.split("\\.");
                sb.append(splitClassName[splitClassName.length - 1]);
            } else {
                sb.append(item);
            }
            if (i != length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
