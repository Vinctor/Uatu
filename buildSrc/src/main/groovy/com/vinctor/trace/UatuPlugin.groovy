package com.vinctor.trace

import com.vinctor.UatuConfig
import org.gradle.api.Plugin
import org.gradle.api.Project;
import com.android.build.gradle.AppExtension

public class UatuPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        project.configurations.all { configuration ->
            def name = configuration.name
            //System.out.println("this configuration is ${name}")
            if (name == "implementation") {
                configuration.dependencies.add(project.dependencies.create("com.vinctor.plugin:uatuCommon:0.1.8"))
            }
        }

        project.extensions.create("UatuConfig", UatuConfig.class,project)

        def android = project.extensions.getByType(AppExtension.class)
        android.registerTransform(new UatuTransform(project))
    }
}
