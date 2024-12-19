---
title: '(2) : Jni 实战 开启Android串口'
date: 2023-09-27 15:34:57
tags: ['Jni', 'Java', 'Kotlin', 'C']
categories:   ['Android', 'Jni', 'C']
---
# 说明

---

**● 在上一篇 文章中我们配置了Jni的基础环境 和相关上层、C层函数声明，接下来就进行编写相关代码 打开串口**

**● 我们先理解下 Android是如何开启串口的**

**● 首先设备是需要root的，这样就可以通过管理员权限获取到串口文件并对文件句柄进行相关操作**

`“文件句柄”听起来是有点抽象 但只用记住 这个一般是 用于管理 文件的相关信息（位置、权限、IO信息等...）`

**● 拿到了 文件句柄后 就可以对串口进行配置、交互等..**

**● 代码核心部分都添加了注释 已经很简洁明了 具体的源码请查看 [KModbus](https://github.com/crowforkotlin/KModbus)**

# 串口查看

---

**● 确保设备已Root，准备ADB 工具，自行配置环境变量**
```shell ● shell 控制台
adb shell
```
```shell ● 获取管理员权限
su
```
```shell ● 显示串口列表
ls /dev | grep ttyS
```
```shell ● 查看某个串口是否占用 假设是 ttyS0 COM1口
lsof /dev/ttyS0
```
# 代码

---

**● [参考LibModbus 代码](https://github.com/stephane/libmodbus/blob/b25629bfb508bdce7d519884c0fa9810b7d98d44/src/modbus-rtu.c#L661)**

### Java Native Interface
```c  ● 打开串口
/*
* Class:     android_serialport_SerialPort
* Method:    open
* Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
*
* Description:
* Opens a serial port and configures its parameters such as baud rate, data bits, stop bits, and parity.
*
* Parameters:
*   path     - The path to the serial port device (e.g., "/dev/ttyS0").
*   baudrate - The desired baud rate (e.g., 9600).
*   flags    - Flags for opening the serial port (e.g., O_RDWR).
*   parity   - Parity mode (0 for none, 1 for even, 2 for odd).
*   stop_bit - Stop bit mode (1 or 2).
*   data_bit - Data bits (5, 6, 7, or 8).
*
* Returns:
*   A FileDescriptor object representing the opened serial port, or NULL if an error occurs.
* @author : revise by crowforkotlin 
* @link : https://github.com/stephane/libmodbus/blob/b25629bfb508bdce7d519884c0fa9810b7d98d44/src/modbus-rtu.c#L661
*/
JNIEXPORT jobject JNICALL Java_com_crow_modbus_serialport_SerialPort_open
(JNIEnv *env, jclass thiz, jstring path, jint baudrate, jint flags, jint parity, jint stop_bit, jint data_bit) {
    LOGD("----------------------------------------------------------");
    int fd;
    speed_t speed;
    jobject mFileDescriptor;

    // 校验波特率 是否正确
    {
        speed = getBaudrate(baudrate);
        if (speed == -1) {
            LOGE("Invalid baudrate！");
            return NULL;
        }
    }

    // 开启串口 O_RDWR 开启读写权限
    {
        jboolean iscopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
        LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
        fd = open(path_utf, O_RDWR | flags);
        LOGD("open() fd = %d", fd);
        (*env)->ReleaseStringUTFChars(env, path, path_utf);
        if (fd == -1) {
            LOGE("Cannot open port");
            return NULL;
        }
    }

    // 配置串口
    {
        struct termios cfg;
        LOGD("Configuring serial port");
        // 尝试获取文件描述符 fd标识 并 存储到 cfg结构体中
        if (tcgetattr(fd, &cfg)) {
            LOGE("tcgetattr() failed");
            close(fd);
            return NULL;
        }

        // 设置原始模式，禁用了自动特殊字符处理、奇偶校验等功能，以便更精确地控制串口数据的传输，但需要应用程序自行处理数据。
        cfmakeraw(&cfg);

        // 设置输入输出波特率
        cfsetispeed(&cfg, speed);
        cfsetospeed(&cfg, speed);

        /* Set data bits (5, 6, 7, 8 bits)
        CSIZE Bit mask for data bits
        */
        cfg.c_cflag &= ~CSIZE;
        switch (data_bit) {
            case 5:
            cfg.c_cflag |= CS5;
            break;
            case 6:
            cfg.c_cflag |= CS6;
            break;
            case 7:
            cfg.c_cflag |= CS7;
            break;
            case 8:
            default:
            cfg.c_cflag |= CS8;
            break;
        }

        /* Stop bit (1 or 2) */
        if (stop_bit == 1)
        cfg.c_cflag &= ~CSTOPB;
        else /* 2 */
        cfg.c_cflag |= CSTOPB;

        // 设置校验位模式       Enable parity bit 
        if (parity == 0) {
            LOGD("NONE");
            cfg.c_cflag &= ~PARENB;
        } else if (parity == 1) {
            LOGD("Even");
            cfg.c_cflag |= PARENB;
            cfg.c_cflag &= ~PARODD;
        } else {
            LOGD("ODD");
            cfg.c_cflag |= PARENB;
            cfg.c_cflag |= PARODD;
        }
        if (tcsetattr(fd, TCSANOW, &cfg)) {
            LOGE("tcsetattr() failed");
            close(fd);
            return NULL;
        }
    }

    // 创建文件描述符
    {
        // 获取类 、 构造函数ID， 字段ID 最后 实例化FileDescriptor 后 并设置字段
        jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
        jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
            jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
            mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
            (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint) fd);
        }

        return mFileDescriptor;
    }
    ```

    ```c  ● 关闭串口
    /*
    * Class:     cedric_serial_SerialPort
    * Method:    close
    * Signature: ()V
    *
    * Description:
    * Closes the serial port by releasing the file descriptor associated with it.
    */
    JNIEXPORT void JNICALL Java_com_crow_modbus_serialport_SerialPort_close(JNIEnv *env, jobject thiz) {
        jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
        jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

        jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFileDescriptor", "Ljava/io/FileDescriptor;");
        jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "fd", "I");

        jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
        jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

        LOGD("close(fd = %d)", descriptor);
        close(descriptor);
    }
    ```

    ### Kotlin Impl
    ```kotlin ● JNI Native声明
    open class SerialPort protected constructor() {

        var mFileDescriptor: FileDescriptor? = null

        companion object {
            init {
                // 加载共享库
                System.loadLibrary("SerialPort")
            }
        }

        /**
        * ● 打开串口
        * @param path 串口文件路径
        * @param baudrate 波特率
        * @param parity 校验
        * @param stopbit 停止位 1 或 2
        * @param databit 数据位 5 - 8
        * ● 2023-09-25 18:29:58 周一 下午
        */
        protected fun open(path: String, baudrate: Int, parity: SerialPortParityFunction, stopbit: Int, databit: Int): FileDescriptor {
            if (stopbit !in 1..2) {
                throw IllegalStateException("stopbit must in 1..2!")
            }
            if (databit !in 5..8) {
                throw IllegalStateException("databit must in 5..8!")
            }
            return open(path, baudrate, 0, parity.code, stopbit, databit)
        }

        // 定义JNI方法，用于打开串口
        private external fun open(path: String, baudrate: Int, flags: Int, parity: Int, stopbits: Int, databits: Int): FileDescriptor

        // 定义JNI方法，用于关闭串口
        protected external fun close()
    }
    ```

    ```kotlin ● 上层串口相关实现
    class SerialPortManager : SerialPort() {

        companion object {
            private val mutex = Mutex()
            private val parentJob = SupervisorJob()
            private val io = CoroutineScope(Dispatchers.IO + parentJob + CoroutineExceptionHandler { coroutineContext, throwable ->
                loggerError("SerialPort an Exception occurs! context is $coroutineContext \t exception : ${throwable.stackTraceToString()}")
            })
        }

        private var mSuccessListener: IOpenSerialPortSuccess? = null
        private var mFailureListener: IOpenSerialPortFailure? = null

        private var mFileInputStream: FileInputStream? = null
        private var mFileOutputStream: FileOutputStream? = null

        private val mReadedBuffer = Bytes(1024)

        /**
        * ● 修改文件权限为可读、可写、可执行
        *
        * ● 2023-09-23 11:41:26 周六 上午
        */
        private fun changeFilePermissions(file: File): Boolean {
            return (file.takeIf { it.exists() } ?: false).runCatching {

                logger("info")

                // 获取ROOT权限
                val su = Runtime.getRuntime().exec("/system/bin/su")

                logger(su)

                // 修改文件属性为 [可读 可写 可执行]
                val cmd = "chmod 777 ${file.absolutePath}\nexit\n"

                // 将命令写入 su 进程的输出流
                su.outputStream.write(cmd.toByteArray())

                // 如果 su 进程返回值为 0 并且文件可读、可写、可执行，则返回 true
                (su.waitFor() == 0 && file.canRead() && file.canWrite() && file.canExecute())
            }
            .onFailure { catch ->
                when (catch) {
                    is IOException -> logger("No root permission!")
                    else -> logger(catch.stackTraceToString())
                }
            }
            .getOrElse { false }
        }

        /**
        * ● 打开串口
        *
        * ● 2023-09-23 16:02:30 周六 下午
        */
        fun openSerialPort(path: String, baudRate: Int) {

            val device = File(path)

            // 校验串口权限
            if (!device.canRead() || !device.canWrite()) {
                if (!changeFilePermissions(device)) {
                    loggerError("openSerialPort : 没有读写权限!")
                    mFailureListener?.onFailure(device, SerialPortState.NO_READ_WRITE_PERMISSION)
                    return
                }
            }

            mFileDescriptor = open(device.absolutePath, baudRate, SerialPortParityFunction.EVEN, 1, 8)
            mFileInputStream = FileInputStream(mFileDescriptor)
            mFileOutputStream = FileOutputStream(mFileDescriptor)
            mSuccessListener?.onSuccess(device)
            logger("openSerialPort : 串口已经打开 $mFileDescriptor")
        }

        /**
        * ● 关闭串口
        *
        * ● 2023-09-23 16:02:12 周六 下午
        */
        fun closeSerialPort(): Boolean {
            return runCatching {
                parentJob.children.forEach { job -> job.cancel() }
                mFileDescriptor = null
                mFileInputStream?.close()
                mFileOutputStream?.close()
                mFileInputStream = null
                mFileOutputStream = null
                mSuccessListener = null
                mFailureListener = null
                true
            }
            .onFailure { catch -> loggerError("close serial port exception! ${catch.stackTraceToString()}") }
            .getOrElse { false }
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun writeBytes(bytes: ByteArray) {
            io.launch {
                mutex.withLock {
                    if (null != mFileDescriptor && null != mFileInputStream && null != mFileOutputStream) {
                        logger("writeBytes ${bytes.map { it.toHexString() }}")
                        mFileOutputStream!!.write(bytes)
                    }
                }
            }
        }

        fun readBytes(iDataReceive: IDataReceive) {
            io.launch {
                while (true) {
                    if (null != mFileDescriptor && null != mFileInputStream && null != mFileOutputStream) {
                        val length = mFileInputStream!!.read(mReadedBuffer)
                        logger("$length")
                        if (length <= 0) return@launch
                        val buffer = Bytes(length)
                        System.arraycopy(mReadedBuffer, 0, buffer, 0, length)
                        iDataReceive.onReceive(buffer)
                    }
                }
            }
        }
    }
    ```