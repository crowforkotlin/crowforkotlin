---
title: '(1) : Jni 简单使用'
date: 2023-09-27 15:33:26
tags: [ 'Jni', 'Java', 'Kotlin', 'C' ]
categories: [ 'Android', 'Jni', 'C' ]
---

# 说明

---

**● 首先是需要在Gradle.kts中配置好(Native)本地环境**

**● 在 对应的工程模块目录下，AndroidStudio默认创建的为 app模块内的src目录下 新建 cpp文件夹 编写C 程序 及 相关头文件**

**● 在上层中 函数名 前面加入(Java) native  (Kotlin) external 关键字声明为C层函数**

**● 一般在类初始化时 通过 System.loadLibrary(...) 加载库**

# 代码

---

1 : app 模块下的 build.gradle.kts

```kotlin
// 配置NDK 
defaultConfig.ndk {
    abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
}

// 配置cmake路径
externalNativeBuild {
    cmake {
        path = file("CMakeLists.txt")
    }
}

```

2 : Cmake 如下

```text
cmake_minimum_required(VERSION 3.4.1)

# 添加库 名称为 SerialPort 的 一个 共享动态库文件 path = .../.../SerialPort.c 
add_library(SerialPort SHARED src/cpp/SerialPort.h src/cpp/SerialPort.c)

# Include libraries needed for libserial_port lib
target_link_libraries(SerialPort android log)
```

3 : 加载动态库

```kotlin
open class SerialPort protected constructor() {

    // 加载共享库
    companion object { init {
        System.loadLibrary("SerialPort")
    }
    }

    // 定义JNI方法，用于打开串口
    private external fun open(
        path: String,
        baudrate: Int,
        flags: Int,
        parity: Int,
        stopbits: Int,
        databits: Int
    ): FileDescriptor

    // 定义JNI方法，用于关闭串口
    protected external fun close()
}
```

```java
public class SerialPort {
    static {
        System.loadLibrary("SerialPort");
    }

    public native static void open();

    public native static void close();
}
```

4 : 使用最新版的AS 自动生成JNI C函数如下所示

```c
// 命名规则如下

// JNIEXPORT jni类型 JNICALL Java_包名_类名_函数名

JNIEXPORT jobject JNICALL Java_com_crow_modbus_serialport_SerialPort_open
(JNIEnv *env, jclass thiz, jstring path, jint baudrate, jint flags, jint parity, jint stop_bit, jint data_bit) { 
    // ....
}
```

**● 对于初学者来说第一眼看到的时候还是很难懂的 所以这里也不涉及Cmake参数配置、gradle的详细配置， 接下来就直接 实现
上层和C层的实现即可非常简单**