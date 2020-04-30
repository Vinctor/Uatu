package com.vinctor.Uatu.test;

import com.vinctor.Uatu.BaseView;

public class Test extends TestGrade implements BaseView {
    String name = "小明";
    int arg = 18;

    public static void test(int x) {
        int i = 1;
        int b = i + 1;
    }

    @Override
    public String toString() {
        return "Test{" +
                "name='" + name + '\'' +
                ", arg=" + arg +
                '}';
    }

    @Override
    public void onCreate() {

    }

    @Override
    public boolean isCorrect() {
        return false;
    }
}
