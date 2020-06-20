package com.vinctor.Uatu;

import com.vinctor.MianBaseView;
import com.vinctor.MyTextUtils;
import com.vinctor.Trace;

public interface BaseView extends MianBaseView {
    void onCreate();

    boolean isCorrect();

    Trace getTrace(MyTextUtils myTextUtils);

    void getArray(String[][] a);
}
