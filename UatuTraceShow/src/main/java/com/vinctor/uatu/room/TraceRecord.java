package com.vinctor.trace.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import static com.vinctor.trace.room.Constant.TRACE_RECORD_TABLE_NAME;

@Entity(tableName = TRACE_RECORD_TABLE_NAME)
public class TraceRecord {

    @PrimaryKey(autoGenerate = true)
    public long id;
    /**
     * 类名
     */
    @ColumnInfo(name = Constant.CLASS_NAME)
    public String className;
    /**
     * 方法名
     */
    @ColumnInfo(name = Constant.METHOD_NAME)
    public String methodName;

    /**
     * 方法参数
     */
    @ColumnInfo(name = "signature")
    public String signature;
    /**
     * 线程
     */
    @ColumnInfo(name = "thread_name")
    public String threadName;
    /**
     * 开始时间戳
     */
    @ColumnInfo(name = Constant.START_TS)
    public Long startTs;

    /**
     * 结束时间戳
     */
    @ColumnInfo(name = Constant.END_TS)
    public Long endTs;

    /**
     * 方法参数
     */
    @ColumnInfo(name = Constant.ARGS)
    public String argments;

    /**
     * 返回数据
     */
    @ColumnInfo(name = Constant.RETURN_)
    public String return_data;
}
