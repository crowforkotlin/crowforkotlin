---
title: Modbus
date: 2023-10-07 10:10:11
tags: [ 'Modbus','协议' ]
categories: [ '协议' ]
---

[//]: # (@formatter:off)

# 入门Modbus协议

---

**● 简单理解下 Modbus 协议, 在Modbus中通信的双方分别是主站（Modbus Poll） 从站（Modbus Slave）**

**● 主站：相当于客户端**

**● 从站：相当于服务端**

**● 通信流程：主站发起请求，从站响应请求。主站向从站发送包含功能码和数据的请求消息，从站根据功能码执行相应的操作，并将响应消息返回给主站。主站负责解析响应消息，从中提取所需的数据**

## 理解特定词汇和概念

---

**● 1字节（位） = 1byte（一个字节）**

**● 串行链路：这是一种数据传输的方式，数据一位一位的按顺序传输，在广域网中使用串行链路提供远距离传输，并且还划分了“异步” 和
“同步”的传输方式， 以字节为单位**

`广域网（Wide Area Network，简称WAN），又称外网、公网。是连接不同地区局域网或城域网计算机通信的远程网。通常跨接很大的物理范围，所覆盖的范围从几十公里到几千公里，它能连接多个地区、城市和国家，或横跨几个洲并能提供远距离通信，形成国际性的远程网络1`

**● PDU：Protocol Data
Unit（协议数据单元）的缩写，是网络通信中传输的数据单位，包括了控制信息和用户数据。在不同的网络层级，PDU有不同的名称，例如在网络层被称为包（Packet），在传输层被称为段（Segment）或者数据报（Datagram），在数据链路层被称为帧（Frame）等。在Modbus协议中，PDU定义了一个与基础通信层无关的简单协议数据单元。**

`● 简单来说，PDU就是网络中传输的数据包，它包含了实际的用户数据以及一些控制信息。`

**● ASCII：（发音：，American Standard Code for Information Interchange，美国信息交换标准代码）和UTF-8一样都是字符编码**

## 通用 功能码

---

|  功能代码  |   中文名称   |            英文名称            |   PLC代码范围    |       中文描述       |                        英文描述                         |
|:------:|:--------:|:--------------------------:|:------------:|:----------------:|:---------------------------------------------------:|
|   01   |  读线圈状态   |      Read Coil Status      | 00001-09999  | 读取离散输出的当前状态（开/关） | Read the current state (on/off) of discrete outputs |
|   02   | 读离散输入状态  | Read Discrete Input Status | 10001-19999  | 读取离散输入的当前状态（开/关） | Read the current state (on/off) of discrete inputs  |
|   03   |  读保持寄存器  |   Read Holding Registers   | 40001-49999  |    读取模拟输出的当前值    |      Read the current value of analog outputs       |
|   04   |  读输入寄存器  |    Read Input Registers    | 30001-39999  |    读取模拟输入的当前值    |       Read the current value of analog inputs       |
|   05   | 写单个线圈寄存器 |     Write Single Coil      | 00001\~09999 |        -         |                  Write Single Coil                  |
|   06   | 写单个保持寄存器 |   Write Single Register    | 40001\~49999 |        -         |                Write Single Register                |
| 0F(15) | 写多个线圈寄存器 |    Write Multiple Coils    | 00001\~09999 |        -         |                Write Multiple Coils                 |
| 10(16) | 写多个保持寄存器 |  Write Multiple Registers  | 40001\~49999 |        -         |              Write Multiple Registers               |

## 认识Modbus数据帧格式

---

### 帧结构PDU
[//]: # (@formatter:on)

| 功能码 | 数据  |
|:---:|:---:|
| 1字节 | 不定长 |

[//]: # (@formatter:off)
### 帧结构MBAP

| 事务处理标识	 | 协议标识 | 长度  | 单元标识符 |
|:-------:|:----:|:---:|:-----:|
|   2字节   | 2字节  | 2字节 |  1字节  |

**● 事务处理标识 可以理解为报文的序列号，一般每次通信之后就要加1以区别不同的通信数据报文**

**● 协议标识符 00 00 表示Modbus TCP协议**

**● 长度 表示接下来的数据长度，单位为字节。**

**● 单元标识符 可以理解为设备地址**

---

**● Modbus的操作对象有四种：线圈、离散输入、保持寄存器、输入寄存器。**

`● 线圈：PLC的输出位，开关量，在Modbus中可读可写`

`● 离散量：PLC的输入位，开关量，在Modbus中只读`

`● 输入寄存器：PLC中只能从模拟量输入端改变的寄存器，在Modbus中只读`

`● 保持寄存器：PLC中用于输出模拟量信号的寄存器，在Modbus中可读可写`

---

**● 提前声明下：根据功能码的不同，帧数据包有不同的结构，RTU、TCP、ASCII的数据帧结构都差不多一致的 除了头尾和校验可能有差异**

### Modbus RTU帧模式 (串行链路PDU)

| 设备地址 | 功能代码 |  数据格式   | CRC16校验 |
|:----:|:----:|:-------:|:-------:|
| 1字节  | 1字节  | N * 1字节 |   2字节   |

**● 读 “单个” 输入寄存器 / 写 “单个” 线圈寄存器**

| 设备地址 | 功能代码 | 起始地址（高位） | 起始地址（低位） | 线圈数量（高位） | 线圈数量（低位） | CRC16校验 |
|:----:|:----:|:--------:|:--------:|:--------:|:--------:|:-------:|   
| 1字节  | 1字节  |   1字节    |   1字节    |   1字节    |   1字节    |   2字节   |

**● 读/写 "多个" 线圈寄存器**

| 设备地址 | 功能代码 | 起始地址（高位） | 起始地址（低位） | 线圈数量（高位） | 线圈数量（低位） | 字节数 | CRC16校验 |
|:----:|:----:|:--------:|:--------:|:--------:|:--------:|:---:|:-------:|
| 1字节  | 1字节  |   1字节    |   1字节    |   1字节    |   1字节    | 1字节 |   2字节   |

### Modbus ASCII帧模式

---

| 帧头  | 设备地址 | 功能码 |     数据      | 校验码LRC | 回车  | 换行  |
|:---:|:----:|:---:|:-----------:|:------:|:---:|:---:|
| 1字符 | 2字符  | 2字符 |     N字符     |  2字符   | 1字符 | 1字符 |
|  ：  |  01  | 03  | 00 00 00 01 |   FB   | \r  | \n  |

### Modbus TCP/IP(ADU) 帧模式

---

**● Modbus TCP/IP(ADU)的数据帧可分为两部分：MBAP + PDU**

| MBAP报文头 | 功能码  |  数据  | 
|:-------:|:----:|:----:|

### 封装前进行梳理

---

`● 1：上面的RTU、TCP、ASCII的每个数据帧格式都是不同的怎么进行封装？`

`● 其实这三种数据帧都有个通用的数据 那就是 (设备地址、功能码、数据) 数据就是 [起始地址的高低位] [线圈数量的高低位] 这里还有个字节数就得根据是什么功能码看是否做对应的添加！`

`● 2：对于CRC校验和LRC校验 可以单独抽出来做个实现即可，根据条件添加`

`● 3：ASCII和TCP的描述都很简陋 实际上可以看RTU就行了`

## 开始封装Modbus通用库

**● [可以参考我自己实现的KModbus](https://github.com/crowforkotlin/KModbus)**

```kotlin 
// RTU
fun build( function: ModbusFunction, slave: Int, startAddress: Int, count: Int, value: Int? = null, values: IntArray? = null): ByteArray {
    val output = buildOutput(slave, function, startAddress, count, value, values)
    toCalculateCRC16(output.toByteArray(), output)
    return output.toByteArray()
}
```

```kotlin
// TCP
private var mTransactionId: Int = 0
private val mProtocol: Int = 0

fun build(function: ModbusFunction, slave: Int, startAddress: Int, count: Int,value: Int? = null, values: IntArray? = null, transactionId: Int = mTransactionId): ByteArray {
    val pdu = buildOutput(slave, function, startAddress, count, value, values, isTcp = true)
    val size = pdu.size()
    val mbap = BytesOutput()
    mbap.writeInt16(transactionId)
    mbap.writeInt16(mProtocol)
    mbap.writeInt16(size + 1)
    mbap.writeInt8(slave)
    mbap.write(pdu.toByteArray())
    mTransactionId++
    return mbap.toByteArray()
}
```

```kotlin
// ASCII
private val HEAD = 0x3A
private val END = byteArrayOf(0x0d, 0x0A)

fun build( function: ModbusFunction, slave: Int, startAddress: Int, count: Int, value: Int? = null, values: IntArray? = null): ByteArray {
    val bytes = BytesOutput()
    val output = buildOutput(slave, function, startAddress, count, value, values).toByteArray()
    val pLRC = fromAsciiInt8(toCalculateLRC(output))
    val outputAscii = toAsciiHexBytes(output)
    bytes.writeInt8(HEAD)
    bytes.writeBytes(outputAscii, outputAscii.size)
    bytes.writeInt8(pLRC.first)
    bytes.writeInt8(pLRC.second)
    bytes.writeBytes(END, END.size)
    return bytes.toByteArray()
}
```


```kotlin
// 核心实现
open class KModbus protected constructor() {

    /**
     * ● 构造输出的数据
     *
     * ● 2023-10-16 16:42:18 周一 下午
     * @author crowforkotlin
     */
    fun buildOutput(slave: Int, function: ModbusFunction, startAddress: Int, count: Int, value: Int?, values: IntArray?, isTcp: Boolean = false): BytesOutput {

        //检查参数是否符合协议规定
        when {
            slave !in 0..0xFF -> throw ModbusException(ModbusErrorType.ModbusInvalidArgumentError, "Invalid slave $slave")
            startAddress !in 0..0xFFFF -> throw ModbusException(ModbusErrorType.ModbusInvalidArgumentError, "Invalid startAddress $startAddress")
            count !in 1..0xFF -> throw ModbusException(ModbusErrorType.ModbusInvalidArgumentError, "Invalid count $count")
        }

        val output = BytesOutput()


        if (!isTcp) {
            output.writeInt8(slave)
        }

        when(function) {

            // 读线圈寄存器 、离散输入寄存器、保持寄存器、输入寄存器
            ModbusFunction.READ_COILS, ModbusFunction.READ_DISCRETE_INPUTS, ModbusFunction.READ_INPUT_REGISTERS, ModbusFunction.READ_HOLDING_REGISTERS -> {
                output.writeInt8(function.mCode)
                output.writeInt16(startAddress)
                output.writeInt16(count)
            }
            ModbusFunction.WRITE_SINGLE_COIL, ModbusFunction.WRITE_SINGLE_REGISTER -> {

                var valueCopy = value ?: throw ModbusException(ModbusErrorType.ModbusInvalidArgumentError, "Function Is $function\t , Data must be passed in!")

                //写单个寄存器指令
                if (function == ModbusFunction.WRITE_SINGLE_COIL) if (value != 0) valueCopy = 0xff00 //如果为线圈寄存器（写1时为 FF 00,写0时为00 00）

                output.writeInt8(function.mCode)
                output.writeInt16(startAddress)
                output.writeInt16(valueCopy)
            }
            ModbusFunction.WRITE_HOLDING_REGISTERS -> {

                //写多个保持寄存器
                output.writeInt8(function.mCode)
                output.writeInt16(startAddress)
                output.writeInt16(count)
                output.writeInt8(2 * count)

                //写入数据
                (values ?: throw ModbusException(ModbusErrorType.ModbusInvalidArgumentError, "Function Is $function, Data must be passed in!")).forEach { output.writeInt16(it) }
            }
            ModbusFunction.WRITE_COILS -> {
                if (values == null || values.isEmpty()) throw ModbusException(ModbusErrorType.ModbusInvalidArgumentError, "Function Is $function, Data must be passed in and cannot be empty!")
                output.writeInt8(function.mCode)
                output.writeInt16(startAddress)
                output.writeInt16(count)
                output.writeInt8((count + 7) shr 3)
                val chunkedValues = values.toList().chunked(8)
                for (chunk in chunkedValues) {
                    output.writeInt8(toDecimal(chunk.reversed().toIntArray()))
                }
            }
        }

        return output
    }

    /**
     * ● CRC校验
     *
     * ● 2023-10-16 16:06:48 周一 下午
     * @author crowforkotlin
     */
    fun toCalculateCRC16(output: BytesOutput): BytesOutput {

        //计算CRC校验码
        output.writeInt16Reversal(CRC16.compute(output.toByteArray()))
        return output
    }

    /**
     * ● LRC校验
     *
     * ● 2023-10-16 16:06:41 周一 下午
     * @author crowforkotlin
     */
    fun toCalculateLRC(data: ByteArray): Int {
        var iTmp = 0
        for (x in data) {
            iTmp += x.toInt()
        }
        iTmp %= 256
        iTmp = (iTmp.inv() + 1) and 0xFF // 对补码取模，确保结果在0-255范围内
        return iTmp
    }


    /**
     * ● Convert each digit component to decimal
     *
     * ● 2023-10-16 16:00:53 周一 下午
     * @author crowforkotlin
     */
    private fun toDecimal(data: IntArray): Int {
        var result = 0
        for (bit in data) {
            if (bit != 0 && bit != 1) {
                return -1  // 数据数组中包含非二进制值，返回错误
            }
            result = (result shl 1) + bit
        }
        return result
    }

}
```

---

## 开源 及 参考

**● [KModbus](https://github.com/crowforkotlin/KModbus)**

**● [LibModbus](https://github.com/stephane/libmodbus/blob/b25629bfb508bdce7d519884c0fa9810b7d98d44/src/modbus-rtu.c#L661)**

**● [知乎-Modbus](https://zhuanlan.zhihu.com/p/484463923)**

**● [CSDN-Modbus](https://blog.csdn.net/tiandiren111/article/details/118347661)**
