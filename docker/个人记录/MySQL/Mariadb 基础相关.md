# 安装与启动

## 安装

### 5.5 版本

```bash
[root@centos ~]# yum install mariadb mariadb-server
```

### 10.x 版本

Centos自带的源，版本可能不是最新的。如果要安装最新版本的mariadb，需要如下操作：

1. 去mariadb官网，官网可以根据你需要的版本生成一个对应的mariadb的repo
2. 复制这个repo的内容，然后进行如下的操作

```bash
[root@centos03 ~]# cd /etc/yum.repos.d
[root@centos03 yum.repos.d]#vim MariaDB.repo 
```

```bash
# MariaDB 10.5 CentOS repository list - created 2020-12-15 06:32 UTC                                                                                 
# https://mariadb.org/download/                                                                                                                      
[mariadb]                                                                                                                                            
name = MariaDB                                                                                                                                       
baseurl = https://mirrors.ustc.edu.cn/mariadb/yum/10.5/centos7-amd64                                                                                 
gpgkey=https://mirrors.ustc.edu.cn/mariadb/yum/RPM-GPG-KEY-MariaDB                                                                                   
gpgcheck=1 
```

```bash
[root@centos03 yum.repos.d]# yum install MariaDB-server MariaDB-client -y
```

之所以要使用较新版本的mariadb，是因为有些比较重要的功能，旧版不支持，比如基于GTID的主从复制，mariadb直到10.0.2才支持

注意：安装完以后，会出现如下两个目录

```bash
[root@centos05 mysql]# cd /var/lib/mysql/
[root@centos05 mysql]# cd /etc/my.cnf.d/  
```

其中 /etc/my.cnf.d/里存的是配置文件

```bash
[root@centos05 my.cnf.d]# ll
total 12
-rw-r--r-- 1 root root  763 Nov 10 08:15 enable_encryption.preset
-rw-r--r-- 1 root root 1080 Nov 10 08:15 server.cnf
-rw-r--r-- 1 root root  120 Nov 10 08:15 spider.cnf
```

默认情况下，现在已经没有了my.cnf文件了。但my.cnf文件依然是实际上的配置文件。
因为官网上写着：MariaDB is normally configured by editing the my.cnf file



## 修改mariadb默认配置

mariadb的默认配置实际上是不适合生产环境的，需要进行调整。

```bash
[mysqld]                                                                                                                                             
datadir=/var/lib/mysql                                                                                                                               
socket=/var/lib/mysql/mysql.sock                                                                                                                     
                                                                                                                                                   
# Disabling symbolic-links is recommended to prevent assorted security risks                                                                         
# Settings user and group are ignored when systemd is used.                                                                                          
# If you need to run mysqld under a different user or group,                                                                                         
# customize your systemd unit file for mariadb according to the                                                                                      
# instructions in http://fedoraproject.org/wiki/Systemd                                                                                              
server-id = 2                                                                                                                                        
log-bin = /var/lib/mysql/mysql-bin                                                                                                                   
log-bin-index = /var/lib/mysql/mysql-bin.index                                                                                                       
relay-log = /var/lib/mysql/mysql-relay                                                                                                               
relay-log-index = /var/lib/mysql/mysql-relay.index                                                                                                   
                                                                                                                                                    
binlog_format = row                                                                                                                                  
                                                                                                                                                   
                                                                                                                                                   
#replicate-do-db             = db%.%                                                                                                                 
# replicate-ignore-db          = mysql.%                                                                                                               
                                                                                                                                                  
replicate-wild-ignore-table = mysql.%,information_schema,performance_schema                                                                          
                                                                                                                                                   
#双1配置
sync_binlog = 1 
innodb_flush_log_at_trx_commit=1

relay_log_recovery = 1                                                                                                                               
log_slave_updates = 1                                                                                                                                
skip-name-resolve = 1                                                                                                                                
                                                                                                                                                  
slow_query_log = ON                                                                                                                                  
slow_query_log_file =/var/lib/mysql/slowquery.log                                                                                                    
long_query_time = 1                                                                                                                                  
                                                                                                                                                     
#character-set-server=utf8                                                                                                                           
innodb-file-per-table=1   
```

一般来说：

1. binlog_format设置为row，虽然插入删除等操作会大量占用空间，但能确保主从一致性
2. 双1设置，sync_binlog = 1，innodb_flush_log_at_trx_commit=1
3. 每个表单独一个文件，innodb-file-per-table=1   

## mariadb的启动

### 5.5 版本

```bash
[root@centos ~]# systemctl start mariadb  //启动mariadb                                                                            
[root@centos ~]# systemctl enable mariadb //设置开机自启动
```

### 10.x版本

如果是10.0.x以后的版本，还需要加点东西：

```bash
[root@centos ~]# systemctl start mariadb  //启动mariadb                                                                            
[root@centos ~]# systemctl enable mariadb //设置开机自启动
```

正常情况下，和5.5版本是一样的，如果有问题，见下面启动中的小问题，你有可能需要执行

```bash
[root@centos03 mysql]# mysql_install_db --user=mysql --basedir=/usr --datadir=/var/lib/mysql
```

这种情况多半是由于你之前没有卸载干净造成的

## mariadb的登陆

mysql -u <user> -p <password> 

```bash
[root@centos ~]# mysql -u root -p
Enter password: 
```

**注意：不要直接在-p后面加密码，否则其它登陆者，可以根据历史记录直接登录，这是一个危险的操作。**

比如使用!mysql就直接登录了

## 启动中的小问题

启动时，如果报错，会提示你，使用 systemctl status mariadb来查看问题原因。我在启动中就遇到这样一个问题：

```bash
[root@centos mysql]# systemctl status mariadb                                                                                    
● mariadb.service
   Loaded: not-found (Reason: No such file or directory)
   Active: failed (Result: exit-code) since Sat 2020-11-21 03:33:11 EST; 13min ago
 Main PID: 89501 (code=exited, status=0/SUCCESS)

Nov 21 03:33:10 centos03 systemd[1]: Starting MariaDB database server...
Nov 21 03:33:10 centos03 mariadb-prepare-db-dir[89467]: Database MariaDB is probably initialized in /var/lib/mysql already,...done.
Nov 21 03:33:10 centos03 mysqld_safe[89501]: 201121 03:33:10 mysqld_safe Logging to '/var/lib/mysql/centos03.err'.
Nov 21 03:33:10 centos03 mysqld_safe[89501]: 201121 03:33:10 mysqld_safe Starting mysqld daemon with databases from /var/lib/mysql
Nov 21 03:33:11 centos03 systemd[1]: mariadb.service: control process exited, code=exited status=1
Nov 21 03:33:11 centos03 systemd[1]: Failed to start MariaDB database server.
Nov 21 03:33:11 centos03 systemd[1]: Unit mariadb.service entered failed state.
Nov 21 03:33:11 centos03 systemd[1]: mariadb.service failed.
Hint: Some lines were ellipsized, use -l to show in full.

```

这么一大段话，其中最关键的就是标红的这句：/var/lib/mysql/centos03.err

然后在这个错误日志里找到了具体的原因，提示找不到mysql-bin.index。
默认情况下，mariadb安装完毕后，在/var/lib/mysql/目录下，是没有任何文件的。而我的系统因为是clone过来的，所以附带了文件，所以只要把/var/lib/mysql/目录下的文件全部清空，再启动就好了

另外，如果是10.0.x版本，可能出现这样的错误：

```bash
[root@centos03 mysql]# systemctl status mariadb
● mariadb.service - MariaDB 10.5.8 database server
   Loaded: loaded (/usr/lib/systemd/system/mariadb.service; disabled; vendor preset: disabled)
  Drop-In: /etc/systemd/system/mariadb.service.d
           └─migrated-from-my.cnf-settings.conf
   Active: failed (Result: exit-code) since Tue 2020-12-15 03:35:27 EST; 7s ago
     Docs: man:mariadbd(8)
           https://mariadb.com/kb/en/library/systemd/
  Process: 70047 ExecStart=/usr/sbin/mariadbd $MYSQLD_OPTS $_WSREP_NEW_CLUSTER $_WSREP_START_POSITION (code=exited, status=1/FAILURE)
  Process: 69982 ExecStartPre=/bin/sh -c [ ! -e /usr/bin/galera_recovery ] && VAR= ||   VAR=`cd /usr/bin/..; /usr/bin/galera_recovery`; [ $? -eq 0 ]   && systemctl set-environment _WSREP_START_POSITION=$VAR || exit 1 (code=exited, status=0/SUCCESS)
  Process: 69980 ExecStartPre=/bin/sh -c systemctl unset-environment _WSREP_START_POSITION (code=exited, status=0/SUCCESS)
 Main PID: 70047 (code=exited, status=1/FAILURE)
   Status: "MariaDB server is down"

Dec 15 03:35:26 centos03 mariadbd[70047]: 2020-12-15  3:35:26 0 [ERROR] Could not open mysql.plugin table: "Table 'mysql.plugin' doesn't exis...ot loaded
Dec 15 03:35:26 centos03 mariadbd[70047]: 2020-12-15  3:35:26 1 [Warning] Failed to load slave replication state from table mysql.gtid_slave_...rectory")
Dec 15 03:35:26 centos03 mariadbd[70047]: 2020-12-15  3:35:26 0 [ERROR] Can't open and lock privilege tables: Table 'mysql.servers' doesn't exist
Dec 15 03:35:26 centos03 mariadbd[70047]: 2020-12-15  3:35:26 0 [Note] Server socket created on IP: '::'.
Dec 15 03:35:26 centos03 mariadbd[70047]: 2020-12-15  3:35:26 0 [ERROR] Fatal error: Can't open and lock privilege tables: Table 'mysql.db' doesn't exist
Dec 15 03:35:26 centos03 mariadbd[70047]: 2020-12-15  3:35:26 0 [ERROR] Aborting
Dec 15 03:35:27 centos03 systemd[1]: mariadb.service: main process exited, code=exited, status=1/FAILURE
Dec 15 03:35:27 centos03 systemd[1]: Failed to start MariaDB 10.5.8 database server.
Dec 15 03:35:27 centos03 systemd[1]: Unit mariadb.service entered failed state.
Dec 15 03:35:27 centos03 systemd[1]: mariadb.service failed.
Hint: Some lines were ellipsized, use -l to show in full.
```

那是因为，mariadb从10.0.x以后，需要预置

```bash
[root@centos03 mysql]# mysql_install_db --user=mysql --basedir=/usr --datadir=/var/lib/mysql
```

之后再启动就没问题了

## 卸载mariadb



# Mariadb 用户

## 查看用户

### 5.5 版本

use mysql;

select host,user,password from user ;

示例：

```mysql
MariaDB [(none)]> use mysql;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
MariaDB [mysql]> select host,user,password from user ;
+-----------+------+-------------------------------------------+
| host      | user | password                                  |
+-----------+------+-------------------------------------------+
| localhost | root |                                           |
| centos03  | root |                                           |
| 127.0.0.1 | root |                                           |
| ::1       | root |                                           |
| localhost |      |                                           |
| centos03  |      |                                           |
| localhost | ggo  | *6BB4837EB74329105EE4568DDA7DC67ED2CA2AD9 |
+-----------+------+-------------------------------------------+
7 rows in set (0.00 sec)

```

### 10.0.x版本

use mysql;

select host,user from user ;

```mysql
MariaDB [(none)]> use mysql
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
MariaDB [mysql]> select host,user from user ;
+-----------+-------------+
| Host      | User        |
+-----------+-------------+
| %         | root        |
| centos03  |             |
| localhost |             |
| localhost | mariadb.sys |
| localhost | mysql       |
| localhost | root        |
+-----------+-------------+
6 rows in set (0.002 sec)
```



## 创建用户

### 5.5 版本

use mysql;

insert into mysql.user(host,user,password) values("%","<user>",password("<password>"));

示例：

```mysql
MariaDB [(none)]> use mysql;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
MariaDB [mysql]> insert into mysql.user(host,user,password) values("localhost","ggo",password("123456"));                         
Query OK, 1 row affected, 4 warnings (0.00 sec)
```

host有两种：localhost（本地）和%（远程）

### 10.x版本

10.0.x以后，将不支持上述方式，因为user变成了一个视图，所以应该采用如下的方式：

  create user if not exists <username>@'<host>' identified by '<password>';

```mysql
MariaDB [(none)]> create user if not exists root@'%' identified by '123456';
Query OK, 0 rows affected (0.002 sec)
```

注意：这种方式都不需要选择库

## 重命名用户

### 10.x版本

rename user <old-user> to <new-user>;

```mysql
MariaDB [mysql]> rename user replica@192.168.0.14 to replica@'%';
Query OK, 0 rows affected (0.006 sec)
```

## 删除用户

### 10.x 版本

drop user if exists <user1>,<user2>,<userN>;

```mysql
MariaDB [mysql]> drop user if exists replica@192.168.0.15;                                                                                               
Query OK, 0 rows affected (0.001 sec)
```



## 修改用户密码

### 5.5版本

set password for '<user>'@'%' = password('<password>');（远程登陆密码）

set password for '<user>'@'localhost' = password('<password>');（本地登陆密码）

示例：

```mysql
MariaDB [mysql]> set password for 'root'@'%' = password('123456');
Query OK, 1 rows affected (0.00 sec)
```

### 10.x版本

alter user 'root'@'localhost' identified by '<password>';

```mysql
MariaDB [mysql]> alter user 'root'@'localhost' identified by '123456';
Query OK, 0 rows affected (0.001 sec)
```



## mariadb 用户权限

### 授予权限

grant <privileges> on <db>.<table> to <user>@"<host>" ;

注意，后面需要跟上：flush privileges;

示例（授予全部权限）：

```mysql
MariaDB [mysql]> grant all on *.* to root@"%";                                                   
Query OK, 0 rows affected (0.00 sec)

MariaDB [mysql]> flush privileges;
Query OK, 0 rows affected (0.00 sec)
```

示例（授予具体的库表权限）：

```mysql
MariaDB [mysql]> grant all on dqcmgr.* to replica@"192.168.0.14";
Query OK, 0 rows affected (0.01 sec)

MariaDB [mysql]> flush privileges;
Query OK, 0 rows affected (0.00 sec)
```

示例：授予增删改查权限

```mysql
MariaDB [(none)]> grant select,insert,update,delete on *.* to root@"%"; 
Query OK, 0 rows affected (0.005 sec)

MariaDB [mysql]> flush privileges;
Query OK, 0 rows affected (0.00 sec)
```

### 撤销权限

revoke <privileges>  on <db>.<table> from <user>@"<host>" 

```mysql
MariaDB [mysql]> revoke all on *.* from root@"%";                                                   
Query OK, 0 rows affected (0.00 sec)

MariaDB [mysql]> flush privileges;
Query OK, 0 rows affected (0.00 sec)
```



# 库、表、字段、索引

## 库相关

### 创建库

```mysql
CREATE DATABASE IF NOT EXISTS consumer
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;
```

## 表相关

### 查看表

```mysql
desc t;
```

### 创建表

```mysql
CREATE TABLE `tuser` (
  `id` int(11) NOT NULL,
  `id_card` varchar(32) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `ismale` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_card` (`id_card`),
  KEY `name_age` (`name`,`age`)
) ENGINE=InnoDB
```

### 表重命名

```mysql
alter table tuser rename as t_user;
```

## 字段相关

### 添加字段

```mysql
alter table t add age varchar DEFAULT 0 COMMENT 'xxxx'
```

### 修改字段

```mysql
alter table t modify age int DEFAULT 0 COMMENT 'xxxx'
```

### 删除字段

```mysql
alter table t drop age;
```

## 索引相关

### 添加索引

```mysql
alter table t add index index_name (column_name);
```

### 删除索引

```mysql
alter table t drop index index_name ;
```

### 修改索引

修改索引可以使用**先删除再添加**来实现;

# 系统变量

## 全局变量

### 查看全局变量

1. select @@global.sort_buffer_size;
2. show global variables like "sort_buffer%";

### 设置全局变量

1. set global sort_buffer_size=16777216;
2. set @@global.sort_buffer_size=16777216;;

## 会话变量

### 查看会话变量

1. select @@sort_buffer_size;
2. select @@session.sort_buffer_size;
3. show variables like "sort_buffer%";
4. show session variables like "sort_buffer%";

 session可加可不加，默认没有就是session变量，下同

### 设置会话变量

1. set @@session.sort_buffer_size=16777216;
2. set sort_buffer_size=16777216;

## 注意事项

1. 通过SQL修改，修改成功即生效。但如果设置的是全局，需要退出当前会话才能生效
2. 有些变量为只读变量，不能通过SQL修改，此时需要在my.cnf内进行配置，然后重启MySQL

## 小结

所有的系统变量，都是可以通过@@来查询的，例如：

```mysql
MariaDB [(none)]> select @@server_id;                                                                                                                    
+-------------+
| @@server_id |
+-------------+
|           1 |
+-------------+
1 row in set (0.002 sec)
```



# Explain相关

```mysql
+------+-------------+-------+-------+---------------+------+---------+------+------+-------------+
| id   | select_type | table | type  | possible_keys | key  | key_len | ref  | rows | Extra       |
+------+-------------+-------+-------+---------------+------+---------+------+------+-------------+
|    1 | SIMPLE      | t     | range | c             | c    | 5       | NULL |    2 | Using where |
+------+-------------+-------+-------+---------------+------+---------+------+------+-------------+
```

## id

SELECT识别符。这是SELECT的查询序列号

**我的理解是SQL执行的顺序的标识，SQL从大到小的执行**

1. id相同时，执行顺序由上至下
2. 如果是子查询，id的序号会递增，id值越大优先级越高，越先被执行
3. id如果相同，可以认为是一组，从上往下顺序执行；在所有组中，id值越大，优先级越高，越先执行

## select_type

1. SIMPLE(简单SELECT，不使用UNION或子查询等)
2. PRIMARY(子查询中最外层查询，查询中若包含任何复杂的子部分，最外层的select被标记为PRIMARY)
3. UNION(UNION中的第二个或后面的SELECT语句)
4. DEPENDENT UNION(UNION中的第二个或后面的SELECT语句，取决于外面的查询)
5. UNION RESULT(UNION的结果，union语句中第二个select开始后面所有select)
6. SUBQUERY(子查询中的第一个SELECT，结果不依赖于外部查询)
7. DEPENDENT SUBQUERY(子查询中的第一个SELECT，依赖于外部查询)
8. DERIVED(派生表的SELECT, FROM子句的子查询)
9. UNCACHEABLE SUBQUERY(一个子查询的结果不能被缓存，必须重新评估外链接的第一行)

## type

对表访问方式，表示MySQL在表中找到所需行的方式，又称“访问类型”。

常用的类型有： **ALL、index、range、 ref、eq_ref、const、system、****NULL（从左到右，性能从差到好）**

1. ALL：Full Table Scan， MySQL将遍历全表以找到匹配的行
2. index: Full Index Scan，index与ALL区别为index类型只遍历索引树
3. range:只检索给定范围的行，使用一个索引来选择行
4. ref: 表示上述表的连接匹配条件，即哪些列或常量被用于查找索引列上的值
5. eq_ref: 类似ref，区别就在使用的索引是唯一索引，对于每个索引键值，表中只有一条记录匹配，简单来说，就是多表连接中使用primary key或者 unique key作为关联条件
6. const、system: 当MySQL对查询某部分进行优化，并转换为一个常量时，使用这些类型访问。如将主键置于where列表中，MySQL就能将该查询转换为一个常量，system是const类型的特例，当查询的表只有一行的情况下，使用system
7. NULL: MySQL在优化过程中分解语句，执行时甚至不用访问表或索引，例如从一个索引列里选取最小值可以通过单独索引查找完成。

## possible_keys

**指出MySQL能使用哪个索引在表中找到记录，查询涉及到的字段上若存在索引，则该索引将被列出，但不一定被查询使用（该查询可以利用的索引，如果没有任何索引显示 null）**

该列完全独立于EXPLAIN输出所示的表的次序。这意味着在possible_keys中的某些键实际上不能按生成的表次序使用。
如果该列是NULL，则没有相关的索引。在这种情况下，可以通过检查WHERE子句看是否它引用某些列或适合索引的列来提高你的查询性能。如果是这样，创造一个适当的索引并且再次用EXPLAIN检查查询

## key

**key列显示MySQL实际决定使用的键（索引），必然包含在possible_keys中**

如果没有选择索引，键是NULL。要想强制MySQL使用或忽视possible_keys列中的索引，在查询中使用FORCE INDEX、USE INDEX或者IGNORE INDEX。

## key_len

**表示索引中使用的字节数，可通过该列计算查询中使用的索引的长度（key_len显示的值为索引字段的最大可能长度，并非实际使用长度，即key_len是根据表定义计算而得，不是通过表内检索出的）**

不损失精确性的情况下，长度越短越好 

## ref

**列与索引的比较，表示上述表的连接匹配条件，即哪些列或常量被用于查找索引列上的值**

## rows

**估算出结果集行数，表示MySQL根据表统计信息及索引选用情况，估算的找到所需的记录所需要读取的行数**

## Extra 

**该列包含MySQL解决查询的详细信息,有以下几种情况：**

1. Using index：表示查询的列，就在索引中。
2. Using index condition：通过条件过滤索引，过滤完索引后找到所有符合索引条件的数据行，随后用 WHERE 子句中的其他条件去过滤这些数据行；
3. Using where：表示Mysql将对storage engine提取的结果进行过滤，过滤条件字段无索引；
4. Using temporary：表示MySQL需要使用临时表来存储结果集，常见于排序和分组查询，常见 group by ; order by
5. Using filesort：当Query中包含 order by 操作，而且无法利用索引完成的排序操作称为“文件排序”
6. Using join buffer：改值强调了在获取连接条件时没有使用索引，并且需要连接缓冲区来存储中间结果。如果出现了这个值，那应该注意，根据查询的具体情况可能需要添加索引来改进能。
7. Impossible where：这个值强调了where语句会导致没有符合条件的行（通过收集统计信息不可能存在结果）。
8. Select tables optimized away：这个值意味着仅通过使用索引，优化器可能仅从聚合函数结果中返回一行
9. No tables used：Query语句中使用from dual 或不含任何from子句



# 性能相关

## show profiles 和show profile

这两个命令，可以查看执行的SQL的具体时间消耗

具体流程如下：

```mysql
MariaDB [test]> set profiling=1;
Query OK, 0 rows affected (0.000 sec)

MariaDB [test]> select * from t;
+----+------+------+
| id | c    | d    |
+----+------+------+
|  0 |    0 |    0 |
|  5 |    5 |    5 |
| 10 |   10 |   10 |
| 15 |   15 |   15 |
| 20 |   20 |   20 |
| 25 |   25 |   25 |
+----+------+------+
6 rows in set (0.001 sec)

MariaDB [test]> show profiles;                                                                                                                         
+----------+------------+-----------------+
| Query_ID | Duration   | Query           |
+----------+------------+-----------------+
|        1 | 0.00040085 | select * from t |
+----------+------------+-----------------+
1 row in set (0.000 sec)

MariaDB [test]> show profile;                                                                                                                          
+------------------------+----------+
| Status                 | Duration |
+------------------------+----------+
| Starting               | 0.000085 |
| checking permissions   | 0.000009 |
| Opening tables         | 0.000106 |
| After opening tables   | 0.000006 |
| System lock            | 0.000004 |
| table lock             | 0.000007 |
| init                   | 0.000017 |
| Optimizing             | 0.000007 |
| Statistics             | 0.000015 |
| Preparing              | 0.000013 |
| Executing              | 0.000002 |
| Sending data           | 0.000062 |
| End of update loop     | 0.000003 |
| Query end              | 0.000030 |
| Commit                 | 0.000003 |
| closing tables         | 0.000002 |
| Unlocking tables       | 0.000003 |
| closing tables         | 0.000004 |
| Starting cleanup       | 0.000002 |
| Freeing items          | 0.000003 |
| Updating status        | 0.000008 |
| Reset for next command | 0.000010 |
+------------------------+----------+
22 rows in set (0.000 sec)
```

上面的情况，只是针对一条SQL语句，如果你继续执行几条语句，就会变成这样：

```mysql
MariaDB [test]> select * from t where id =5;                                                                                                             
+----+------+------+
| id | c    | d    |
+----+------+------+
|  5 |    5 |    5 |
+----+------+------+
1 row in set (0.002 sec)

MariaDB [test]> select * from t where id =10;
+----+------+------+
| id | c    | d    |
+----+------+------+
| 10 |   10 |   10 |
+----+------+------+
1 row in set (0.000 sec)

MariaDB [test]> show profiles;                                                                                                                           
+----------+------------+------------------------------+
| Query_ID | Duration   | Query                        |
+----------+------------+------------------------------+
|        1 | 0.00040085 | select * from t              |
|        2 | 0.00155804 | select * from t where id =5  |
|        3 | 0.00028632 | select * from t where id =10 |
+----------+------------+------------------------------+
4 rows in set (0.000 sec)

```

此时，如果直接show profile，默认是最后一条的具体信息。如果要指定某一条，应该这样做：

```mysql
MariaDB [test]> show profile for query 2;
+------------------------+----------+
| Status                 | Duration |
+------------------------+----------+
| Starting               | 0.000038 |
| Opening tables         | 0.000003 |
| After opening tables   | 0.000008 |
| Query end              | 0.000003 |
| Commit                 | 0.000002 |
| closing tables         | 0.000001 |
| Starting cleanup       | 0.000002 |
| Freeing items          | 0.000003 |
| Updating status        | 0.000005 |
| Reset for next command | 0.000011 |
+------------------------+----------+
10 rows in set (0.000 sec)
```

show profiles里会显示具体的Query_ID，指定query id即可

show profiles还有更多用法，比如CPU，IO占用等等，具体的可以查看官方的文档。

**警告：MySQL官网已经提示，show profile在以后的版本会被逐步地废弃，将会使用performance_schema库的一些语句来替代。**

# 附录

## Mariadb 基本使用流程

1. 安装mariadb
2. 修改mariadb默认配置
3. 启动mariadb
4. 登陆mariadb，默认用户名为root，密码没有设置
5. 创建mariadb用户
6. 设置新用户的远程和本地登陆权限
7. 授权数据库和数据表给新用户

## 附录2. 相关问题

### 导入视图存在的问题

导入sql时，如果存在视图，可能会出现如下错误：The user specified as a definer ('root'@'%') does not exist

这是因为视图是存在definer的

![](./相关图片/视图definer.jpg)

当你从别的库上导出视图时，其它库上创建这个视图的用户可能和要导入的目标数据库的用户名不一样。

### 添加索引耗时1小时

```mysql
MariaDB [test]> alter table t add index d (d);                                                                                             
Query OK, 0 rows affected (1 hour 14 min 5.20 sec)
Records: 0  Duplicates: 0  Warnings: 0
```

执行alter语句添加，发现卡着不动，此时执行show processlist，提示：Waiting for table metadata lock 

```mysql
MariaDB [(none)]> show processlist;                                                                                                                   
+----+---------+---------------------+------+-------------+--------+-----------------------------------------------------------------------+-------------------------------+----------+
| Id | User    | Host                | db   | Command     | Time   | State                                                                 | Info                          | Progress |
+----+---------+---------------------+------+-------------+--------+-----------------------------------------------------------------------+-------------------------------+----------+
| 22 | replica | 192.168.0.14:48488  | NULL | Binlog Dump | 360806 | Master has sent all binlog to slave; waiting for binlog to be updated | NULL                          |    0.000 |
| 31 | root    | localhost           | test | Sleep       |  65936 |                                                                       | NULL                          |    0.000 |
| 32 | root    | localhost           | test | Sleep       |  66889 |                                                                       | NULL                          |    0.000 |
| 47 | root    | localhost           | test | Query       |     18 | Waiting for table metadata lock                                       | alter table t add index d (d) |    0.000 |
| 48 | root    | localhost           | NULL | Query       |      0 | NULL                                                                  | show processlist              |    0.000 |
+----+---------+---------------------+------+-------------+--------+-----------------------------------------------------------------------+-------------------------------+----------+

```

这种情况通常是有其它线程正在对该表进行操作，占用了MDL锁

我们看到，由于id=31和id=32这两行的Command是Sleep，我们不清楚，到底是哪个占用了MDL锁，想直接查找也比较困难。但是有了 performance_schema 和 sys 系统库以后，这个问题就容易了：

```mysql
select blocking_pid from sys.schema_table_lock_waits;
```

注意：mariadb没有sys库，无法直接用这个。sys库实际上是一些列视图，其数据全部来自于performance_schema

小结：这个案例的重点在于，当你试图添加索引时，发现被阻塞的时候，如何排查，通常遇到此类问题，首先应该想到的，就是使用show processlist命令

### RR模式下防幻读锁全表

```mysql
CREATE TABLE `t` (  
  `id` int(11) NOT NULL,  
  `c` int(11) DEFAULT NULL,  
  `d` int(11) DEFAULT NULL,  
  PRIMARY KEY (`id`),  
  KEY `d` (`d`)) ENGINE=InnoDB;
  
insert into t values(0,0,0),(5,5,5),(10,10,10),(15,15,15),(20,20,5),(25,25,25);
```

表和数据如上。

| 事务A                                                        | 事务B                                        |
| ------------------------------------------------------------ | -------------------------------------------- |
| begin;                                                       |                                              |
| select * from t where d=5 for update; //Q1，结果为（5，5，5）,（20，20，5） |                                              |
|                                                              | insert into t values (100,100,100);//blocked |

如上面表格所示。

1. 表t，d字段有索引。事务A执行Q1，试图锁住d=5的行，以及他们之间的间隙。
2. 按照正常的理解，d=5的两行，id=5，id=20被锁住，并且d的值处于(5,10]这个间隙也被锁住。
3. 我们知道，防幻读的根本原理，就是间隙锁。而间隙锁此时的范围是(5,10]，所以，此时我们试图插入id=100，c=100，d=100这个值，是不会被锁住的。但事实是，他被锁住了。为什么呢？

答：select * from t where d=5，这一句，并没有走索引，而走的是全表扫描！！！

你可能会奇怪，字段d上明明有索引，怎么会走全表扫描？答案很简单，因为数据太少了，由于d是覆盖索引，所以select * 需要回表，在数据少的情况下，优化器认为，与其走索引，再回表，还不如直接走全表扫描来得快。

我们可以看下explain的结果：

```mysql
MariaDB [test]> explain select * from t where d=5;                                                                                                    
+------+-------------+-------+------+---------------+------+---------+------+------+-------------+
| id   | select_type | table | type | possible_keys | key  | key_len | ref  | rows | Extra       |
+------+-------------+-------+------+---------------+------+---------+------+------+-------------+
|    1 | SIMPLE      | t     | ALL  | d             | NULL | NULL    | NULL |   13 | Using where |
+------+-------------+-------+------+---------------+------+---------+------+------+-------------+
1 row in set (0.00 sec)
```

那如果我希望走索引，该怎么办？一个比较简单的方式，是把select * 改成select id 或者select id,d

```mysql
MariaDB [test]> explain select id from t where d=5;
+------+-------------+-------+------+---------------+------+---------+-------+------+-------------+
| id   | select_type | table | type | possible_keys | key  | key_len | ref   | rows | Extra       |
+------+-------------+-------+------+---------------+------+---------+-------+------+-------------+
|    1 | SIMPLE      | t     | ref  | d             | d    | 5       | const |    4 | Using index |
+------+-------------+-------+------+---------------+------+---------+-------+------+-------------+
1 row in set (0.00 sec)
```

这个时候，就不会阻塞了

| 事务A                                                        | 事务B                                   |
| ------------------------------------------------------------ | --------------------------------------- |
| begin;                                                       |                                         |
| select id from t where d=5 for update; //Q1，结果为（5，5，5）,（20，20，5） |                                         |
|                                                              | insert into t values (100,100,100);//OK |

## 附录3. 配置优化相关

### 双1设置

**sync_binlog** =1

**innodb_flush_log_at_trx_commit**=1

回顾下相关的选项：

sync_binlog用于控制每个线程把自己 binlog cache中的binlog内容write到page cache或者fsync到磁盘文件

1. sync_binlog=0 的时候，表示每次提交事务都只 write，不 fsync；
2. sync_binlog=1 的时候，表示每次提交事务都会执行 fsync；
3. sync_binlog=N(N>1) 的时候，表示每次提交事务都 write，但累积 N 个事务后才 fsync。

 innodb_flush_log_at_trx_commit 用于控制redo log的写入策略

1. 设置为 0 的时候，表示每次事务提交时都只是把 redo log 留在 redo log buffer 中 ;
2. 设置为 1 的时候，表示每次事务提交时都将 redo log 直接持久化到磁盘；
3. 设置为 2 的时候，表示每次事务提交时都只是把 redo log 写到 page cache

为了保证数据的准确性和安全性，通常配置为双1

### 并发线程上限

在 InnoDB 中，innodb_thread_concurrency 这个参数的默认值是 0，表示不限制并发线程数量。但是，不限制并发线程数肯定是不行的。因为，一个机器的 CPU 核数有限，线程全冲进来，上下文切换的成本就会太高

**所以，通常情况下，我们建议把 innodb_thread_concurrency 设置为 64~128 之间的值**

你可能会有疑问，并发线程上限数设置为 128 够干啥，线上的并发连接数动不动就上千了

实际上，你搞混了**并发连接**和**并发查询**

并发连接和并发查询，并不是同一个概念。你在 show processlist 的结果里，看到的几千个连接，指的就是并发连接。而“当前正在执行”的语句，才是我们所说的并发查询。

并发连接数达到几千个影响并不大，就是多占一些内存而已。我们应该关注的是并发查询，因为并发查询太高才是 CPU 杀手。这也是为什么我们需要设置 innodb_thread_concurrency 参数的原因

这时，你可能问，那么出现同一行热点更新的问题时，是不是很快就把 128 消耗完了，这样整个系统是不是就挂了呢？

实际上，在线程进入锁等待以后，并发线程的计数会减一，也就是说等行锁（也包括间隙锁）的线程是不算在 128 里面的

### join buffer的配置

join buffer 主要用在两个地方：

1. NLJ算法进一步优化成BKA算法时，需要用到join buffer
2. BNL算法需要用到join buffer

join buffer的默认值是256K。

调大的原因，是因为数据量大的时候，BKA和BNL需要对数据进行分段，分段次数越多，扫描表的次数越多。被驱动表的数据是M行，那么分段K次的话，就是K\*M次，而这个K的大小就是join buffer决定的。

join buffer越大，分段的次数就越小，但受限于物理机器内存的大小，这个值也不能设太大。

配置的话，对应的参数值是join_buffer_size

### MRR配置

MRR的优化，主要出现在普通索引范围查找，然后需要通过主键回表拿数据的场景。因为按主键顺序读数据，是顺序读，所以速度会快，因此开启MRR后，会对普通索引查询到的结果集进行排序，然后按主键的顺序去回表取数据。

开启MRR，需要设置

```mysql
set optimizer_switch='mrr=on,mrr_cost_based=off';
```

对结果集排序需要额外开辟一块内存，这个大小被称为read_rnd_buffer_size，这个值的默认大小也是256K。

### BKA算法配置

BKA算法，其实就是NLJ + MRR，只不过它是用的join_buffer_size

开启BKA算法，首先要开启MRR。总的方法是

```mysql
set optimizer_switch='mrr=on,mrr_cost_based=off,batched_key_access=on';
```

上面的这种方式是会话级别的，所以你只需要在执行SQL之前执行这个就行。这个会话结束以后，不会影响其他SQL，只对这个会话的SQL生效。

### innodb_buffer_pool_size

InnoDB Buffer Pool 的大小是由参数 **innodb_buffer_pool_size** 确定的，一般建议设置成可用**物理内存的 60%~80%**

### sort_buffer_size

我们知道排序，如果有索引，就直接拿索引的结果集。没有索引的话，MySQL就要把查询到的数据，放到一个内存中进行排序，这内存，就是sort buffer

sort_buffer用的是快速排序，当这个内存不够用的时候，就会采用外部排序，外部排序使用的是归并排序，归并排序会把要排序的数据分成N分，保存到磁盘临时文件上分别进行排序运算然后再汇总，这样的话性能就会大大下降。

所以需要适当调整sort_buffer_size的值。

sort_buffer_size默认值是2M