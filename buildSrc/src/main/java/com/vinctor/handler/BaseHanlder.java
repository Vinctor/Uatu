package com.vinctor.handler;

import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.ide.common.blame.parser.aapt.AbstractAaptOutputParser;

import java.util.Collection;

public abstract class BaseHanlder {
    TransformInvocation transformInvocation;

    public BaseHanlder(TransformInvocation transformInvocation) {
        this.transformInvocation = transformInvocation;
        init();
    }

    private void init() {
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
    }


    abstract void onHanlerFileInput();

    abstract void onHanlerJarInput();
}
