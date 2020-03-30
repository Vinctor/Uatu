package com.vinctor.uatu.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {TraceRecord.class}, version = 1)
public abstract class UatuDatabase extends RoomDatabase {

    public abstract TraceRecordDao getTraceRecoedDao();


    private static final Object sLock = new Object();
    private static UatuDatabase INSTANCE;

    public static UatuDatabase init(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE =
                        Room.databaseBuilder(context.getApplicationContext(), UatuDatabase.class, "uatu.db")
                                .allowMainThreadQueries()
                                .build();
            }
            return INSTANCE;
        }
    }

    public static UatuDatabase getInstance() {
        return INSTANCE;
    }
}
