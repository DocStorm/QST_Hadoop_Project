# Round 1 介绍
方案介绍：
    解压每个文件夹里的access.log.gz文件，利用正则表达式过滤得到每一行的ip，访问地址后存入新文件，以日期命名。将这些新文件放入一个文件夹内并上传到HDFS供使用。
    1.map中以IP为Key，value值为1。reduce中将接收的从map发送的KV对存入set集合去重，所得结果即为UV，并写入新文件保存。
    2.map中以访问地址为Key，ip为value。在reduce中将value值也就是IP放入set集合中去重（由于key是访问地址，所以分桶时会将相同key的放在一起，所以集合中元素个数即为该key也就是访问地址的访问量，也就是pv。
    3.将第二天的uv数据通过追加v重定向写入到第一天的数据后。重复第一种代码运行，由于set集合的存在会把留存的去重得到的结果为两天的UV。两天的UV之和减去两天的UV就是留存量。
环境搭建：
    1.配置Java环境：
        vim /home/hadoop/.bashrc 最后一行添加 export JAVA_HOME=/home/hadoop/jdk1.6.0_45/ 
        export CLASSPATH=.:$CLASSPATH:$JAVA_HOME/lib:$JRE_HOME/lib 
        export PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin 
        输入source ~/.bashrc 
        测试Java配置是否成功：Java-version 
    2.安装Hadoop：
        配置相应的文件（修改master文件、slaves文件、core-site.xml、mapred-site.xml、hdfs-site.xml、hadoop-env.sh、sudo vi /etc/hosts） 
        配置成功后复制该虚拟机两到三台 
        建立互信关系：在master生成公私钥ssh-keygen 
             复制公钥cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys 
             在slave机器上输入ssh-keygen 
             从master机器上复制公钥scp master:~/.ssh/authorized_keys /home/hadoop/.ssh/ 
             测试连接：在master机器上分别向所有的slave机器发起联接请求
             启动Hadoop：sbin目录下./start-all.sh 
             查看进程是否正常启动：jps
