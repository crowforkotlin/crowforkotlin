```
1. (利用blutter脚本)[https://github.com/crowforkotlin/blutter]

2. 这里blutter有坑，需要gcc 13版本以上, 使用wsl 测试debian，下载了gcc 13 利用blutter 对arm64-v8a so进行反编译产生一堆错误

3. 选择使用wsl Ubuntu-24.04 流程如下
wsl -list -online
wsl --install Ubuntu-24.04
wsl --distribution Ubuntu-24.04
sudo passwd root
su
apt-get update
sudo apt install gcc-13 g++-13

这里可以尝试设置gcc默认版本
sudo update-alternatives --config gcc
sudo update-alternatives --config g++

apt install python3-pyelftools python3-requests git cmake ninja-build \build-essential pkg-config libicu-dev libcapstone-dev
python3 blutter.py arm64-v8a/ out_dir

4. 如果配置正确将会生成一个out_dir 里面则包含反编译出来的dart文件
```
