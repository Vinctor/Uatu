package com.vinctor.Uatu.test;

public class Test extends TestGrade {
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
}
