# Uatu

用于项目中方法替换和方法的跟踪(查看参数, 返回值, 方法耗时等).

## 用法

一:
root project `build.gradle`中添加:

```
classpath com.vinctor.plugin:uatu:0.2.0
```
二:
app module `build.gradle`中:

应用:
```
apply plugin: com.vinctor.uatu.UatuPlugin
```
添加配置:
```
UatuConfig {
    enable = true
    traceConfig {
        enable = true
        logAllArgs = true
        traceClass = 'com.vinctor.Trace'
        tracePackage = ['com.vinctor']
        exclude = ['com.vinctor.Uatu.test.TestExclude']
    }

    replaceConfig {
        enable = true
        configFile="replaceFile.json"
    }
}
```
配置参数说明:
```
UatuConfig->enable :插件配置是否可用(false:traceConfig和replaceConfig都不可用)
```
配置分为两部分: 方法跟踪和方法替换

1. 方法跟踪traceConfig:
```
enable : 是否可用
logAllArgs : 是否输出方法的参数
traceClass : 方法跟踪输出类(全限定名), 由使用者自行实现, 方法
tracePackage : 要跟踪的类列表(全限定名)
exclude : tracePackage中要排出的类列表
```


