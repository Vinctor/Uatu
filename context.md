# Uatu Trace 功能上下文文档

## 概述

Uatu 是一个 Android 字节码插桩插件，Trace 功能用于在编译期自动为指定包名下的方法注入埋点代码，实现方法调用追踪。

## 架构流程

```
编译时流程:
┌─────────────────┐     ┌──────────────────────┐     ┌─────────────────────┐
│ UatuPlugin      │ --> │ UatuTransform        │ --> │ TransformHandler    │
│ (注册Transform) │     │ (扫描ITraceListener) │     │ (处理每个class文件) │
└─────────────────┘     └──────────────────────┘     └─────────────────────┘
                                  │
                                  v
                        ┌──────────────────────┐
                        │ TraceListenerScanner │
                        │ (查找ITraceListener  │
                        │  实现类)             │
                        └──────────────────────┘

字节码修改:
┌─────────────────┐     ┌─────────────────────┐
│ UatuClassVisitor│ --> │ TraceMethodVisitor  │
│ (类级别判断)    │     │ (方法级别插桩)      │
└─────────────────┘     └─────────────────────┘
```

## 核心文件

### 插件配置
- `buildSrc/src/main/java/com/vinctor/UatuConfig.java` - 主配置类
- `buildSrc/src/main/java/com/vinctor/trace/TraceConfig.java` - Trace配置

### Transform 处理
- `buildSrc/src/main/groovy/com/vinctor/trace/UatuTransform.groovy` - Transform入口
- `buildSrc/src/main/java/com/vinctor/handler/TransformHandler.java` - 字节码处理
- `buildSrc/src/main/java/com/vinctor/handler/BaseHanlder.java` - 基础处理器（含增量编译）
- `buildSrc/src/main/java/com/vinctor/handler/TraceListenerScanner.java` - ITraceListener扫描器

### 字节码生成
- `buildSrc/src/main/java/com/vinctor/trace/UatuClassVisitor.java` - 类访问器，生成TraceUtil单例
- `buildSrc/src/main/java/com/vinctor/trace/TraceMethodVisitor.java` - 方法访问器，注入start/end调用

### 运行时库 (uatuCommon)
- `uatuCommon/src/main/java/com/vinctor/plugin/uatulib/trace/ITraceListener.java` - 接口定义
- `uatuCommon/src/main/java/com/vinctor/plugin/uatulib/trace/TraceUtil.java` - 运行时工具类（编译时生成单例）
- `uatuCommon/src/main/java/com/vinctor/plugin/uatulib/trace/DefaultUatuTrace.java` - 默认实现（已移除接口实现）

## 配置使用

```groovy
// build.gradle
apply plugin: 'uatu'

UatuConfig {
    enable = true
    traceConfig {
        enable = true
        jarEnable = false           // 是否处理JAR中的类
        logAllArgs = true           // 是否记录方法参数
        tracePackage = ['com.example']  // 需要追踪的包名
        exclude = []                // 排除的类
    }
}
```

## ITraceListener 接口

```java
public interface ITraceListener {
    /**
     * 方法进入时调用
     * @return 唯一标识，传递给end方法（可以是任意类型：String/Long/自定义对象）
     */
    Object start(String className, String method, String signature, Object[] args);

    /**
     * 方法退出时调用
     * @param id start方法返回的标识
     */
    void end(Object id, String className, String method, String signature, Object returnObj);
}
```

## 自定义实现示例

```kotlin
class MyTraceListener : ITraceListener {
    override fun start(className: String, method: String, signature: String, args: Array<Any>): Any {
        // 直接返回时间戳作为ID（巧妙：不需要Map存储）
        return SystemClock.elapsedRealtime()
    }

    override fun end(id: Any, className: String, method: String, signature: String, returnObj: Any) {
        val cost = SystemClock.elapsedRealtime() - (id as Long)
        if (cost > 100) {  // 只记录耗时超过100ms的方法
            Log.d("Trace", "$className#$method cost: ${cost}ms")
        }
    }
}
```

## 插桩生成的代码

原始方法：
```java
public void foo() {
    // 业务代码
}
```

插桩后：
```java
public void foo() {
    ITraceListener listener = TraceUtil.getIntance();
    Object id = listener.start("com/example/MyClass", "foo", "", new Object[]{});
    try {
        // 业务代码
    } finally {
        listener.end(id, "com/example/MyClass", "foo", "", null);
    }
}
```

## TraceUtil 单例生成

`UatuClassVisitor.generateTraceUtilSingleton()` 在编译时为 TraceUtil 生成：

```java
public class TraceUtil {
    private static final ITraceListener sInstance = new MyTraceListener();
    
    public static ITraceListener getIntance() {
        return sInstance;
    }
}
```

## 关键优化

1. **按需扫描**: 只在非增量构建时扫描 ITraceListener 实现类
2. **早退出优化**: `TransformHandler.handlerClass()` 不满足条件时直接返回原始字节
3. **增量编译**: 支持增量编译，只处理变化的文件
4. **编译时单例**: TraceUtil 使用静态字段初始化，避免运行时创建对象

## 注意事项

1. 自定义 ITraceListener 实现类会被自动发现，无需配置
2. 如果没有找到 ITraceListener 实现，插桩会被跳过
3. `DefaultUatuTrace` 已移除 `implements ITraceListener`，不会被扫描器识别
4. `tracePackage` 配置的包名会自动转换为内部格式（`.` -> `/`）

## 版本兼容

- Gradle: 7.2+
- AGP: 7.1.3
- ASM: 9.6 (支持 Java Records)
- 使用 `Opcodes.ASM9` API 版本

## 最近修改 (2026-04)

1. 移除 Replace 功能，只保留 Trace
2. ITraceListener 返回值从 String 改为 Object（更灵活）
3. 添加 TransformHandler 早退出优化
4. 修复 checkFileName 过滤逻辑（R类/BuildConfig类）
5. 实现增量编译支持
6. 移除 DefaultUatuTrace 的接口实现
