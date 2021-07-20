##### 1.ldd 显示命令依赖的动态链接库

查看某个命令，用到了哪些动态链接库

```bash
[root@centos03 /]# ldd /usr/bin/bash
        linux-vdso.so.1 =>  (0x00007ffcd864f000)
        libtinfo.so.5 => /lib64/libtinfo.so.5 (0x00007f5aadf75000)
        libdl.so.2 => /lib64/libdl.so.2 (0x00007f5aadd71000)
        libc.so.6 => /lib64/libc.so.6 (0x00007f5aad9a2000)
        /lib64/ld-linux-x86-64.so.2 (0x000055fc7175b000)
```

##### 2.cp 多个文件

```bash
[root@centos03 lib64]# cp /lib64/{libtinfo.so.5,libdl.so.2,libc.so.6} .
[root@centos03 lib64]# ll
total 2300
-rwxr-xr-x 1 root root 2156160 Sep 14 07:55 libc.so.6
-rwxr-xr-x 1 root root   19288 Sep 14 07:55 libdl.so.2
-rwxr-xr-x 1 root root  174520 Sep 14 07:55 libtinfo.so.5
```

##### 3.chroot 切换根目录

切换根目录以后，被切换的目录，就成了根目录，一切都以这个目录为起点。不影响原先的一切。

```bash
drwxr-xr-x 2 root root  1024 Sep 14 08:16 bin
drwxr-xr-x 2 root root  1024 Sep 14 08:17 lib64
drwx------ 2 root root 12288 Sep 14 08:12 lost+found
[root@centos03 myroot]# chroot ./                                          
bash-4.2# echo "hello world" > /123.txt
bash-4.2# exit
exit
[root@centos03 myroot]# ll
total 15
-rw-r--r-- 1 root root    12 Sep 14 08:18 123.txt
drwxr-xr-x 2 root root  1024 Sep 14 08:16 bin
drwxr-xr-x 2 root root  1024 Sep 14 08:17 lib64
drwx------ 2 root root 12288 Sep 14 08:12 lost+found
```

我们看到，chroot 到myroot目录以后，在根目录下创建一个123.txt，实际上是在myroot目录下创建了一个123.txt

##### 4.lsof 显示进程打开了哪些文件

```bash
[root@centos03 myroot]# lsof -p $$                                                                                       
COMMAND    PID USER   FD   TYPE DEVICE  SIZE/OFF     NODE NAME
bash    106107 root  cwd    DIR    7,0      1024        2 /mnt/myroot
bash    106107 root  rtd    DIR  253,0       274       64 /
bash    106107 root  txt    REG  253,0    960472 50407572 /usr/bin/bash
bash    106107 root  mem    REG  253,0 106075056  1185089 /usr/lib/locale/locale-archive
bash    106107 root  mem    REG  253,0     61624   332808 /usr/lib64/libnss_files-2.17.so
bash    106107 root  mem    REG  253,0   2156160  1185096 /usr/lib64/libc-2.17.so
bash    106107 root  mem    REG  253,0     19288   332795 /usr/lib64/libdl-2.17.so
bash    106107 root  mem    REG  253,0    174520   341147 /usr/lib64/libtinfo.so.5.9
bash    106107 root  mem    REG  253,0    163400  1185088 /usr/lib64/ld-2.17.so
bash    106107 root  mem    REG  253,0     26254 16881916 /usr/lib64/gconv/gconv-modules.cache
bash    106107 root    0u   CHR  136,0       0t0        3 /dev/pts/0
bash    106107 root    1u   CHR  136,0       0t0        3 /dev/pts/0
bash    106107 root    2u   CHR  136,0       0t0        3 /dev/pts/0
bash    106107 root  255u   CHR  136,0       0t0        3 /dev/pts/0
```

lsof -p $$ （注：$$表示的是当前bash的pid）

其中FD这一列，代表了文件描述符。

第一行：表示的是系统当前所在的目录。
第二行：表示的系统的根目录所在
第三行：表示的是该进程启动时用的可执行文件，所在的位置

第四 - 十行：即FD = mem这几行，表示的是该进程分配的内存空间，都对应哪些文件。

第十一  - 十三行：即FD = 0u，1u，2u这几行，表示的是标准输入、输出和错误

第十四行：即FD = 255u这一行

##### 5.{} 执行代码块

```bash
[root@centos03 /]# { echo "123"; echo "456"; }                                                                           
123
456
```

注意，左括号之后必须跟空格，右括号之前也必须跟空格。每条命令必须带分号，表示一条命令的结束。

##### 6.netstat -natp

##### 7.tcpdump 抓包

```bash
[root@centos03 ~]# tcpdump -nn -i ens33 port 9090
tcpdump: verbose output suppressed, use -v or -vv for full protocol decode
listening on ens33, link-type EN10MB (Ethernet), capture size 262144 bytes
22:55:41.359960 IP 192.168.0.187.50250 > 192.168.0.13.9090: Flags [S], seq 1970582845, win 65535, options [mss 1460,nop,wscale 6,nop,nop,TS val 236812719 ecr 0,sackOK,eol], length 0
22:55:41.360004 IP 192.168.0.13.9090 > 192.168.0.187.50250: Flags [S.], seq 2428113028, ack 1970582846, win 1152, options [mss 1460,sackOK,TS val 1462020212 ecr 236812719,nop,wscale 0], length 0
22:55:41.360099 IP 192.168.0.187.50250 > 192.168.0.13.9090: Flags [.], ack 1, win 2058, options [nop,nop,TS val 236812719 ecr 1462020212], length 0
```

##### 8.man命令

man = manual，手册的意思。

+ man man

  文档有很多类，可以先查看自己，有几类

  ```bash
  The table below shows the section numbers of the manual followed by the types of pages they contain.
  
         1   Executable programs or shell commands
         2   System calls (functions provided by the kernel)
         3   Library calls (functions within program libraries)
         4   Special files (usually found in /dev)
         5   File formats and conventions eg /etc/passwd
         6   Games
         7   Miscellaneous (including macro packages and conventions), e.g. man(7), groff(7)
         8   System administration commands (usually only for root)
         9   Kernel routines [Non standard]
  ```

  

+ man bash

  很多bash的用法，例如，&&这种方式，其实都是来自于这个手册，应该说这个手册的帮助是很大的。

  

+ man tcp

  如果想了解tcp的相关事宜，通过这个命令可以更好的了解。根据分类指示，如果想看tcp的一些方法，可以使用以下的一些例子：

  ```bash
  [root@centos03 socket]# man 2 socket
  ```

  ```bash
  SOCKET(2)                                     Linux Programmer's Manual                               
  
  NAME
         socket - create an endpoint for communication
  
  SYNOPSIS
         #include <sys/types.h>          /* See NOTES */
         #include <sys/socket.h>
  
         int socket(int domain, int type, int protocol);
  
  DESCRIPTION
         ....
  ```

  



##### 9.strace -ff -o

```bash
[root@centos03 socket]# strace -ff -o out java TestSocketIO
```

```bash
[root@centos03 socket]# ll
total 316
-rwxr--r-- 1 root root     45 Sep 16 22:35 client.sh
-rw-r--r-- 1 root root   9477 Sep 18 04:29 out.4554
-rw-r--r-- 1 root root 177682 Sep 18 04:29 out.4555
-rw-r--r-- 1 root root   3141 Sep 18 04:29 out.4556
-rw-r--r-- 1 root root    921 Sep 18 04:29 out.4557
-rw-r--r-- 1 root root   1045 Sep 18 04:29 out.4558
-rw-r--r-- 1 root root    965 Sep 18 04:29 out.4559
-rw-r--r-- 1 root root   5530 Sep 18 04:29 out.4560
-rw-r--r-- 1 root root   3109 Sep 18 04:29 out.4561
-rw-r--r-- 1 root root    921 Sep 18 04:29 out.4562
-rw-r--r-- 1 root root  38158 Sep 18 04:29 out.4563
-rwxr--r-- 1 root root     59 Sep 16 22:31 server.sh
-rw-r--r-- 1 root root   1218 Sep 17 10:54 SocketClient.class
-rw-r--r-- 1 root root    899 Sep 17 10:52 SocketClient.java
-rw-r--r-- 1 root root   3255 Sep 17 10:54 SocketIOPropertites.class
-rw-r--r-- 1 root root   4269 Sep 17 10:41 SocketIOPropertites.java
-rwxr--r-- 1 root root     60 Sep 18 04:29 test.sh
-rw-r--r-- 1 root root   2006 Sep 18 04:29 TestSocketIO.class
-rw-r--r-- 1 root root   1049 Sep 18 04:26 TestSocketIO.java
```

strace常用来跟踪进程执行时的系统调用和所接收的信号。 在Linux世界，进程不能直接访问硬件设备，当进程需要访问硬件设备(比如读取磁盘文件，接收网络数据等等)时，必须由用户态模式切换至内核态模式，通 过系统调用访问硬件设备。strace可以跟踪到一个进程产生的系统调用，包括参数，返回值，执行消耗的时间。

##### 10.route -n 查看路由表

```bash
[root@centos03 socket]# route -n
Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
0.0.0.0         192.168.0.1     0.0.0.0         UG    100    0        0 ens33
192.168.0.0     0.0.0.0         255.255.255.0   U     100    0        0 ens33
192.168.122.0   0.0.0.0         255.255.255.0   U     0      0        0 virbr0
```



##### 11.ulimit 查看修改单进程允许打开的文件描述符数量

##### 12 nc 命令 模拟TCP客户端和服务端

nc 192.168.0.187 9090









