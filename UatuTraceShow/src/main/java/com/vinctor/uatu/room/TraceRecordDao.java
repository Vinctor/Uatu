package com.vinctor.uatu.room;

import android.net.wifi.aware.PublishConfig;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.util.StringUtil;

import java.util.List;

import static com.vinctor.uatu.room.Constant.ORDER_BY_START_TS_DESC;
import static com.vinctor.uatu.room.Constant.TRACE_RECORD_TABLE_NAME;

@Dao
public interface TraceRecordDao {

    @Query("delete from " + TRACE_RECORD_TABLE_NAME)
    int deleteAll();

    @Insert
    public long insertRecord(TraceRecord record);

    @Query("select * from " + TRACE_RECORD_TABLE_NAME + ORDER_BY_START_TS_DESC)
    public List<TraceRecord> getAll();

    @Query("select * from " + TRACE_RECORD_TABLE_NAME + " where "
            + Constant.CLASS_NAME + " like '%'||:filterString||'%'"
            + " or "
            + Constant.METHOD_NAME + " like '%'||:filterString||'%'"
            + ORDER_BY_START_TS_DESC)
    public List<TraceRecord> getAllByClassOrMethod(String filterString);

    @Query("select * from " + TRACE_RECORD_TABLE_NAME + " where "
            + Constant.CLASS_NAME + " like '%'||:classFileter||'%'"
            + " and "
            + Constant.METHOD_NAME + " like '%'||:methodFilter||'%'"
            + ORDER_BY_START_TS_DESC)
    public List<TraceRecord> getAllByClassAndMethod(String classFileter, String methodFilter);
}
