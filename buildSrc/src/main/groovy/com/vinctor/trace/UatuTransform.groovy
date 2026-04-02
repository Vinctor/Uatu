package com.vinctor.trace

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.vinctor.log.Log
import com.vinctor.UatuConfig
import com.vinctor.UatuContext
import com.vinctor.handler.TraceListenerScanner
import com.vinctor.handler.TransformHandler
import org.gradle.api.Project

@SuppressWarnings("deprecation")
class UatuTransform extends Transform {

    private Project project
    UatuConfig config;
    UatuContext context;

    UatuTransform(Project project) {
        this.project = project
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        this.config = project.UatuConfig;
        config.traceConfig = project.traceConfig;
        config.init()
        
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        boolean isIncremental = transformInvocation.isIncremental()
        
        // 非增量时清空输出
        if (!isIncremental && outputProvider != null) {
            outputProvider.deleteAll()
        }
        
        context = new UatuContext(config)

        // 非增量时扫描 ITraceListener 实现类
        if (!isIncremental && config.traceConfig != null && config.traceConfig.enable) {
            Log.e("UatuTransform", "TraceListenerScanner start");
            TraceListenerScanner.start(transformInvocation)
        }

        Log.i("UatuTransform", "isIncremental: " + isIncremental);

        String traceClass = TraceConfig.getTraceClassFromGraph(config.traceConfig);
        if (traceClass == null) {
            Log.e("UatuTransform", "No ITraceListener found, skip instrumentation");
            config.traceConfig.enable = false;
        } else {
            config.traceConfig.setTraceClass(traceClass);
        }
        Log.e(config.toString())
        
        // 传递增量参数
        TransformHandler.start(transformInvocation, config, context, isIncremental);
    }
}
