# 一主一从

## 前置说明

1. 将以1主1从的案例，来记录相关操作
2. 将覆盖两种主从方式：基于位点和基于GTID的方式
3. 所有节点，默认都已经安装好mariadb，且版本一致
4. 主节点，默认已经存在数据。

前置准备：

1. 主节点：192.168.0.13
2. 从节点：192.168.0.14

mariadb版本：10.5.8

关于从节点：从节点，可以是一个完成的空实例，然后从Master复制所有的Binlog，也可以是基于Master某个时间点的全量备份。

通常来说，基于某个时间点的全量备份，来做主从复制是比较常见的。所以后面的细节，也以这个为例。

## 基本流程

### 以空实例做从节点

这种方式，就是建一个完全没有任何数据的从节点，然后从Master复制所有的Binlog。

然而在实际生产环境中这几乎是不可能的，因为最早的Binlog文件应该早就被清除了。但这里也说明一下

1. 主库：创建从库账号，有几个从库，就创建几个，
2. 主库：对从库账号进行授权
3. 从库：设定从主库同步的位置（change master 操作）
4. 从库：启动slave
5. 从库：查看slave状态

### 从备份集做从节点

一般来说创建Slave的方式都是通过备份集来恢复出一个新的实例，然后找到Master上复制的起始点创建复制关系

1. 主库：创建从库账号，有几个从库，就创建几个，
2. 主库：对从库账号进行授权
3. 主库：导出现有数据
4. 主库：将导出的数据发送给从库
5. 从库：导入从主库接受到的数据
6. 从库：设定从主库同步的位置（change master 操作）
7. 从库：启动slave
8. 从库：查看slave状态

## 具体细节

### 主库：创建从库账号

语法：create user if not exists <replicauser>@'<host>' identified by '<password>';  

示例：

```mysql
MariaDB [(none)]> use mysql;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
MariaDB [mysql]> create user if not exists replica@'192.168.0.14' identified by '123456';                       
Query OK, 1 row affected, 4 warnings (0.00 sec)
```

这个host，指的是从库的host

这里如果想简单点用一个账号，建议使用'%'

```mysql
MariaDB [mysql]> create user if not exists replica@'%' identified by '123456';                       
Query OK, 1 row affected, 4 warnings (0.00 sec)
```



### 主库：对从库账号进行授权

语法：

1. grant  replication slave on *.* to "<replicauser>"@"<ip>" identified by "<password>";
2. flush privileges;

示例：

```mysql
MariaDB [mysql]> grant  replication slave on *.* to "replica"@"192.168.0.14";
Query OK, 0 rows affected (0.01 sec)

MariaDB [mysql]> flush privileges;
Query OK, 0 rows affected (0.00 sec)
```

### 主库：导出数据

#### 基于位点的导出

如果主从是基于位点来做，那么导出的时候，简单地导出就可以了：

mysqldump -h <MasterHost> -u <user> -p --database <db> > /<path>/<xxx>.sql

一般来说，导数据，建议在从库上执行命令，不要直接登录主库的机器去导数据。所以加上-h <MasterHost>

示例：

单个库

```bash
[root@centos03 ~]# mysqldump -u root -p dqcmgr > /root/dqcmgr.sql
```

多个库

```bash
[root@centos03 ~]# mysqldump -u root -p --database dqcmgr test > /root/dqcmgr.sql
```

全部库

```bash
[root@centos03 ~]# mysqldump -u root -p --all-databases > /root/dqcmgr.sql
```

示例为了简单一些，所以没加-h

#### 基于GTID的导出

如果主从是基于位点来做，那么导出的时候，必须带上GTID

mysqldump -u <user> -p  --master-data=1 --gtid --single-transaction --database <db> > /<path>/<xxx>.sql

示例：

这里只展示多个库，其它单个或者全部，参考上面

```bash
[root@centos03 ~]# mysqldump  -u root -p  --master-data=1 --gtid --single-transaction --databases dqcmgr test > /root/dump.sql

```

推荐使用基于GTID的导出，这样就不需要自己去找位点了

### 主库：传输数据给从库

这个就是简单的scp命令

```bash
[root@centos03 ~]# scp /root/dqcmgr.sql root@192.168.0.14:/root                                                                    
root@192.168.0.14's password: 
dqcmgr.sql
```

### 从库：关闭binlog

```mysql
[mysqld]                                                                                                                                             
datadir=/var/lib/mysql                                                                                                                               
socket=/var/lib/mysql/mysql.sock                                                                                                                                                                                                                 
server-id = 2       

#log-bin = /var/lib/mysql/mysql-bin                                                                                                                  
#log-bin-index = /var/lib/mysql/mysql-bin.index                                                                                                                                                                                                        
                                                                                                                                                     
#binlog_format = row                                                                                                                                 
                                                                                                                                                   
                                                                                                                                                     
```

主要就是#这几项，注释掉就可以了

### 从库：导入从主库接受到的数据

语法：mysql -u <user> -p < /<path>/<xxx>.sql

示例：

```bash
[root@centos ~]# mysql -u root -p < /root/dump.sql
```

### 从库：设置全局只读

```mysql
set global read_only=1;
```

### 从库：设定从主库同步的位置

#### 基于位点的设定

change master to 
master_host='<host>',
master_user='<user>',
master_password='<password>',
master_log_file='mysql-bin.<index>',
master_log_pos=<pos>;

这里master_log_file和master_log_pos怎么来？
答：在主库上执行show master status\G

```mysql
MariaDB [(none)]> show master status;
+------------------+----------+--------------+------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB |
+------------------+----------+--------------+------------------+
| mysql-bin.000004 |      245 |              |                  |
+------------------+----------+--------------+------------------+
1 row in set (0.00 sec)
```

#### 基于GTID的设定

change master to 
master_host='<host>',
master_user='<user>',
master_password='<password>',
master_use_gtid={current_pos | slave_pos | no };

通常而言，如果是新的从节点，接入主库，master_use_gtid设置为slave_pos即可

示例：

```mysql
MariaDB [(none)]> change master to  master_host='192.168.0.13', master_user='replica', master_password='123456', master_use_gtid=slave_pos;
```

### 从库：启动slave

```mysql
MariaDB [(none)]> start slave;                                                                                                     
Query OK, 0 rows affected (0.01 sec)
```

### 从库：查看slave状态

```mysql
MariaDB [(none)]> show slave status\G;
....
Slave_IO_Running: Yes
Slave_SQL_Running: Yes
....
```

出现这个状态，说明主从搭建完成。然后找个不用的表，插入一些数据验证一下即可。

## 问题

### 从库做了变更，导致数据不一致该怎么办？

答：没有办法，必须删除数据，重做。所以从库必须要设成只读的。

### 从库只复制了一个库，主库在另外一个库上做了修改，导致从库出现问题怎么办？

答：从库在设置主从同步的时候，要设置白名单，表示只同步哪些库

### 如果change master的误操作了怎么办？

这种误操作，有可能是：

1. 在master上做了change master操作
2. slave选择点位的时候出现问题
3. 其它各类原因导致start slave失败

这时候记住2个操作：

1. stop slave
2. reset slave

**要注意，reset master这个行为要千万慎重，他会清理掉binlog。如果不是重做数据库，千万不要用**。

reset master 只是用于重新从0开始搭建主备，主从，需要清理掉原先的binlog时才会使用。

### show master status 显示的是几个文件的内容？

答：1个。

### 为什么从库设置为只读，依然还能插入数据？

答：因为登陆的账号的权限可能是all，拥有all权限的账号依然可以执行各种操作。所以谨慎设置all权限。

# 双主双从

## 相关说明

1. 准备4个节点，2个作为主节点，2个作为从节点
2. 按照上面一主一从的方式，把4个节点都弄好
3. 两个主节点互相chang master到对方
4. 两个主节点执行start slave

所谓双主双从，其实就是一主一备多从。

# 使用Mycat

## 为什么要使用mycat？

上面搭建主从的方式，无论是一主一从，还是双主双从，都存在如下几个问题：

问题一：双主，即主备的情况下，主节点挂了，如何切换到备用节点？
说明：客户端的连接默认都是指向主节点的，如果没有一个中间件，就要在客户端预存主备节点的信息

问题二：如果要做读写分离，如何做到写请求指向主节点，读请求指向从节点
说明：跟上面的问题类似，如果没有一个中间件，就要在客户端对读写做分类。

另外，如果业务量过大，需要做分库分表的话，也是要用到mycat的

这里，我们重点说明mycat如何用来管理读写分离

## 下载、安装mycat

下载地址：http://www.mycat.org.cn/

下载完，解压即可。不需要额外的操作

## 配置mycat

mycat有很多配置文件，如果我们就做主从和读写分离的话，只需要改其中两个文件即可
他们分别是：server.xml、schema.xml  

### server.xml

默认的server.xml里有很多内容，把多余的删掉。改成下面的就可以了

```xml
<?xml version="1.0" encoding="UTF-8"?>                                                                                                                                                                                                                                                       
<!DOCTYPE mycat:server SYSTEM "server.dtd">                                                                                                                  
	<mycat:server xmlns:mycat="http://io.mycat/">                                                                                                                
			<user name="root" defaultAccount="true">                                                                                                             
            <property name="password">123456</property>                                                                                                  
             <property name="schemas">dqcmgr</property>                                                                                                   
              <property name="defaultSchema">dqcmgr</property>                                                                                             
      </user>                                                                                                                                              
	</mycat:server> 
```

### schema.xml

```xml
<?xml version="1.0"?>                                                                                                                                          
<!DOCTYPE mycat:schema SYSTEM "schema.dtd">                                                                                                                    
<mycat:schema xmlns:mycat="http://io.mycat/">                                                                                                                  
                                                                                                                                                               
        <schema name="dqcmgr" checkSQLschema="true" sqlMaxLimit="100" dataNode="dn1">                                                                          
        </schema>                                                                                                                                              
        <dataNode name="dn1" dataHost="localhost1" database="dqcmgr" />                                                                                        
        <dataHost name="localhost1" maxCon="1000" minCon="10" balance="1"                                                                                      
                          writeType="0" dbType="mysql" dbDriver="native" switchType="1"  slaveThreshold="100">                                                 
                 <heartbeat>select user()</heartbeat>                                                                                                           
                 <!-- can have multi write hosts -->                                                                                                            
                 <writeHost host="hostM1" url="192.168.0.13:3306" user="root"  password="123456">                                                               
                         <readHost host="hostS1" url="192.168.0.14:3306" user="root" password="123456">		      
                   			 </readHost>                                              
                </writeHost>                                                                                                                                   
                <writeHost host="hostM2" url="192.168.0.15:3306" user="root"  password="123456">                                                               
                        <readHost host="hostS2" url="192.168.0.16:3306" user="root" password="123456">
                  			</readHost>                                              
                  </writeHost>                                                                                                                                   
          </dataHost>                                                                                                                                            
  </mycat:schema>

```

schema标签的name，表示的是逻辑库，这个名字可以随便取。dataNode里的database才是真正指向的物理库

writeHost和readHost分别表示的写节点和读节点。

dataHost有两个属性值需要说明下

参数balance：表示如何进行读写分离

1. balance=0，不开启读写分离机制，所有读操作都发送到当前可用的writehost上
2. balance=1，全部的readhost和stand by writehost参与select 语句的负载均衡，简单的说，当双主双从模式下，其他的节点都参与select语句的负载均衡
3. balance=2，所有读操作都随机的在writehost，readhost上分发
4. balance=3，所有读请求随机的分发到readhost执行，writehost不负担读压力 

参数switchType：表示如何进行主备切换：

1. switchType=1，默认值，自动切换
2. switchType=-1:表示不自动切换
3. switchType=2：基于mysql主从同步的状态决定是否切换

## 启动mycat

进入mycat_home的bin目录，执行./mycat start

如果要以控制台的方式启动，执行./mycat console

如果要关闭，执行./mycat stop

按照上述方式配置好mycat以后，在主备多从的情况下，就可以实现主备切换和读写分离。

## 防火墙打开8066和9066端口

记住打开这两个端口，否则无法访问

# 高可用

## 相关说明

使用mycat以后，实现了mysql主备的高可用。但是如果mycat挂掉了呢？是不是整个体系就不能用了？

如果要为了实现mycat的高可用。需要引入haproxy和keepalived

总体示意图如下：

![](/Users/soleray/Documents/我的学习/个人记录/MySQL/相关图片/mycat高可用.png)

## haproxy

### 下载haproxy

去官网下载的话，极其慢，这里推荐一个镜像站点

https://www.newbe.pro/Mirrors/Mirrors-HAProxy/

可以下载各个版本的haproxy，建议使用1.8版本，不要用太高版本，会有很多坑

### 安装haproxy

```bash
cd /usr/local/haproxy-1.8.25/
uname -r #查看内核版本
make TARGET=linux26 #编译
make install PREFIX=/usr/local/haproxy #安装
```

### 配置haproxy

```bash
mkdir -p /var/data/haproxy
mkdir /usr/local/haproxy/conf
vim /usr/local/haproxy/haproxy.conf
```

向配置文件中添加配置信息

```bash
global
		log 127.0.0.1 local2
		#log 127.0.0.1 local1 notice
		#log loghost local0 info
		maxconn 4096
		chroot /usr/local/haproxy #重要
		pidfile /var/data/haproxy/haproxy.pid #重要
		uid 99
		gid 99
		daemon
		#debug
		#quiet
defaults
		log global
		mode tcp
		option abortonclose
		option redispatch
		retries 3
		maxconn 2000
		timeout connect 5000
		timeout client 50000
		timeout server 50000
listen proxy_status
	bind :48066 #重要
		mode tcp
		balance roundrobin
		server mycat_1 192.168.85.111:8066 check inter 10s #重要
		server mycat_2 192.168.85.112:8066 check inter 10s #重要
frontend admin_stats
	bind :7777
		mode http
		stats enable
		option httplog
		maxconn 10
		stats refresh 30s
		stats uri /admin
		stats auth admin:123123
		stats hide-version
		stats admin if TRUE
```

配置文件里，列出了几个重要的点，其中两个server就是用来配置mycat的节点

### 设置haproxy的日志

我们在上面的文件看到，log 127.0.0.1 local2这样一行，也就是说默认日志输出到local2这个设备，但因为没有配置，实际上haproxy默认没有日志

需要如下这样做：

```bash
vim /etc/rsyslog.conf
```

找到最下面，添加local2.*   /var/log/haproxy.log（默认情况下只有local7.*  /var/log/boot.log 这一行）

```bash
# Save boot messages also to boot.log                                                                                                                          
local7.*                                                /var/log/boot.log                                                                                      
local2.*                                                /var/log/haproxy.log  
```

重启rsyslog

```bash
systemctl restart rsyslog
```

后续启动haproxy，就能在/var/log/ 目录下，找到haproxy.log 了。

### 启动haproxy

```bash
/usr/local/haproxy/sbin/haproxy -f /usr/local/haproxy/conf/haproxy.conf
```

查看haproxy的进程，如果存在则说明没有问题

```bash
ps -ef | grep haproxy
```

### 防火墙打开相关端口

防火墙打开48066和7777端口

然后打开浏览器访问如下页面，用户名为admin，密码为123123

```
http://192.168.0.15:7777/admin
```

这个ip就是haproxy所在的ip

接着你就能看到mycat的管理情况

## keepalived

### 下载keepalived

官网:http://keepalived.org

下载后直接解压



### 安装keepalived

进入解压后的目录

```bash
./configure --prefix=/usr/local/keepalived
make && make install
```



### 修改配置文件

```bash
mkdir /etc/keepalived
cp /usr/local/keepalived/etc/keepalived/keepalived.conf /etc/keepalived/
cd etc/keepalived/
vim keepalived.conf
```

```bash
! Configuration File for keepalived
vrrp_instance VI_1 {
    state MASTER #重要
    interface eth0 #重要
    virtual_router_id 51
    priority 100
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        192.168.0.100/24 dev eth0 label eth0:3 #重要
    }
}
```

配置文件有3个地方比较重要

1. state，keepalived通常是两个，1个是MASTER，一个是BACKUP
2. interface，这个是网卡的设备，通过ifconfig可以看到，不同的机器不一样，不要写错
3. virtual_ipaddress，这是虚拟地址。要按照上面的例子来写，注意，eth0，就是第二点提到的网卡名称。要一致。

之前有个怪事，192.168.0.100配置好以后可以访问，改成99就不能访问了。

### 把keepalived注册成系统服务

我们要把keepalived注册成系统服务，这样启动比较方便

```bash
cp /usr/local/keepalived-1.4.5/keepalived/etc/sysconfig/keepalived /etc/sysconfig/
cp /usr/local/keepalived/sbin/keepalived /usr/sbin/
```

### 设置keepalived的日志

默认情况下，keepalived的日志是混在/var/log/message里的，这样看肯定不方便

所以要做如下操作：

```bash
vim /etc/sysconfig/keepalived
```

把KEEPALIVED_OPTIONS改成如下，默认只有-D

```bash
KEEPALIVED_OPTIONS="-D -d -S 0"
```

同样，接着还要修改rsyslog

```bash
vim /etc/rsyslog.conf
```

找到最下面，添加local0.*   /var/log/keepalived.log（默认情况下只有local7.*  /var/log/boot.log 这一行）

```bash
# Save boot messages also to boot.log                                                                                                                          
local7.*                                                /var/log/boot.log                                                                                      
local0.*                                                /var/log/keepalived.log  
```

重启rsyslog

```bash
systemctl restart rsyslog
```

在Centos7下，还需要修改keepalived.service

```bash
vim /lib/systemd/system/keepalived.service
```

```bash
[Unit]                                                                                                                                                         
Description=LVS and VRRP High Availability Monitor                                                                                                             
After=network-online.target syslog.target                                                                                                                      
Wants=network-online.target                                                                                                                                    
                                                                                                                                                               
[Service]                                                                                                                                                      
Type=forking                                                                                                                                                   
PIDFile=/run/keepalived.pid                                                                                                                                    
KillMode=process                                                                                                                                               
#EnvironmentFile=-/usr/local/keepalived/etc/sysconfig/keepalived                                                                                               
#ExecStart=/usr/local/keepalived/sbin/keepalived $KEEPALIVED_OPTIONS                                                                                           
EnvironmentFile=/etc/sysconfig/keepalived                                                                                                                      
ExecStart=/sbin/keepalived $KEEPALIVED_OPTIONS                                                                                                                 
ExecReload=/bin/kill -HUP $MAINPID                                                                                                                             
                                                                                                                                                               
[Install]                                                                                                                                                      
WantedBy=multi-user.target  
```

主要是改了EnvironmentFile和ExecStart

接着再启动的话，就能看到/var/log下面就有单独的keepalived.log了

### 启动keepalived

```bash
systemctl start keepalived
systemctl status keepalived
```

### 说明

两个keepalived是一样配置的，只要把其中一个节点的state改成BACKUP就可以了

## 小结

这里可能有一个疑问，haproxy到底是个啥，为什么要用他来代理mycat？

其实一个比较好的对比，就是拿haproxy和nginx做对比。当然两者的定位是不一样的。
nginx本身就是一个web服务器，可以处理各种请求。
haproxy则侧重要与转发与负载均衡。

对比下两种高可用方案：

1. keepalived + nginx + tomcat
2. keepalived + haproxy + mycat

是不是能引发一些共鸣呢

# mycat-web

通过mycat-web可以对mycat服务进行监控。

这个比较简单

1. 下载安装zookeeper，什么都不用改，直接启动
2. 从mycat官网下载mycat-web，解压，什么都不用改，直接启动
3. 访问 http://192.168.0.13:8082/mycat，这个host就是你安装的mycat的机器

# 关于一些坑

## 使用GTID从空实例做主从复制的坑

存在两个问题：

1.主库mysql-bin.000001文件包含了一些mysql表初始化的GTID事务。从库虽然设置了Replicate_Ignore_DB和Replicate_Wild_Ignore_Table，但从库同步的时候依然会去执行这些事务。

解决方案：目前证实可行的方案有两个：

1. 不要从空实例开始，而是从一个备份的节点开始。当然，导出这个备份需要带GTID。
2. 如果一定要从空实例开始，那么就找到真正开始写数据的GTID，通常而言，是从mysql-bin.000002开始的。通过set global gtid_slave_pos = "<gtid>"。来实现空实例的主从复制。

2.要同步的库，必须存在，不存在同样会报错。

