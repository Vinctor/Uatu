package com.vinctor.Uatu;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.vinctor.Uatu.test.Test;
import com.vinctor.Uatu.test.TestExclude;
import com.vinctor.plugin.uatulib.TraceLog;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        add(1, 1);
        new TestExclude().hashCode();
    }

    private int add(int i, int i1) {
        return i + i1;
    }

    @Override
    protected void onStart() {
        super.onStart();
        test();
        new Thread(new Runnable() {
            @Override
            public void run() {
                add(2, 3);
            }
        }).start();
    }

    void test2(long var1, Test test) {
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    void test() {
        String i = getid("123");

        test2(1, new Test());

        use(i);
    }

    String getid(String sss) {
        return "1";
    }

    void use(String id) {

    }
}
