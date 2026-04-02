package com.vinctor.trace

import com.vinctor.UatuConfig
import org.gradle.api.Plugin
import org.gradle.api.Project;
import com.android.build.gradle.AppExtension

public class UatuPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        // 注释掉自动添加依赖，由用户在 build.gradle 中显式添加
        // 避免与本地项目依赖冲突
         project.configurations.all { configuration ->
             def name = configuration.name
             if (name == "implementation") {
                 configuration.dependencies.add(project.dependencies.create("com.yuanfudao.android:uatuCommon:0.0.3"))
             }
         }

        project.extensions.create("UatuConfig", UatuConfig.class, project)

        def android = project.extensions.getByType(AppExtension.class)
        android.registerTransform(new UatuTransform(project))
    }
}
