package com.vinctor.uatu.room;

public class Constant {
    //sql
    public static final String TRACE_RECORD_TABLE_NAME = "trace_record";
    public static final String START_TS = "start_ts";
    public static final String END_TS = "end_ts";
    public static final String CLASS_NAME = "class_name";
    public static final String METHOD_NAME = "method_name";
    public static final String ARGS = "args";
    public static final String RETURN_ = "return_";

    //参数初始化
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    //核心线程数量大小
    public static final int corePoolSize = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    //线程池最大容纳线程数
    public static final int maximumPoolSize = CPU_COUNT * 2 + 1;
    //线程空闲后的存活时长
    public static final int keepAliveTime = 30;


    //查找全部本地方法记录
    public static final int CHECK_ALL_RECORD = 0X01;

    public static final String ORDER_BY_START_TS_DESC = " order by " + START_TS + " DESC";
}
