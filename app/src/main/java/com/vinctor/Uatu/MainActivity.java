package com.vinctor.Uatu;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.vinctor.Uatu.test.Test;
import com.vinctor.Uatu.test.TestAnnotation;
import com.vinctor.Uatu.test.TestExclude;
import com.vinctor.plugin.uatulib.TraceLog;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        log("onCreate");
        TextUtils.isEmpty("123");
        new TestExclude().hashCode();
    }

    @Override
    protected void onStart() {
        super.onStart();
        test();
        log("onStart");
    }

    void test2(long var1, Test test) {
    }

    @Override
    protected void onStop() {
        super.onStop();
        log("onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        log("onPause");
    }

    @TraceLog
    public void log(String msg) {
        Log.d("main", msg);
    }

    void test() {
        String i = getid("123");

        test2(1, new Test());

        use(i);
        testThread();
    }

    @TraceLog
    private long testThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log("run in thread");
            }
        }).start();
        return 54544555L;
    }

    @TestAnnotation
    String getid(String sss) {
        return "1";
    }

    void use(String id) {

    }
}
