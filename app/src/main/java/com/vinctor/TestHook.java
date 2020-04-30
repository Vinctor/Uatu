package com.vinctor;

import android.text.TextUtils;

import com.vinctor.Uatu.BaseView;
import com.vinctor.Uatu.test.Test;
import com.vinctor.Uatu.test.TestExclude;

public class TestHook {

    public static void test() {
        String sss = "111";
        TestExclude view = new TestExclude();
        view.onCreate();
        view.isCorrect();
        Exception e = new Exception("测试抛出异常");
        e.printStackTrace();

        TestExclude view2 = new TestExclude();
        view2.onCreate();
        view2.isCorrect();
    }
}
