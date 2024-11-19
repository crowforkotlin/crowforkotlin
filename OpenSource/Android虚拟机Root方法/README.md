- **1.开源 RootAvd[https://github.com/crowforkotlin/rootAVD]** 
  
```shell
# 1.首先选择运行的虚拟机，创建后建议重新命名，例如RootAvdXX  RootAvd25

# 2.我这里测试过Android API 35、34，中途都遇到了一些麻烦的问题，最后选择的是API 25  Android 7.1  这里类型最好选择架构64位类型Google API

# 3.参考RootAvd使用，选择对应的镜像，完成后会自动安装Magisk。打开后统一权限等待5秒自动重启

# 4.参考这里的设置!(notes)[https://github.com/crowforkotlin/rootAVD?tab=readme-ov-file#automotive-notes]

# 5.勾选启用Magisk，Magisk App -> Settings -> Multiuser Mode -> User-Independent -> reboot AVD 后在magisk选择reboot

# 6.继续参考上述配置，下面通过 emulator -writable-system -avd Avd71 运行允许写入系统权限的avd，后续通过mount，remount挂载对应的内容即可增删改系统文件
```