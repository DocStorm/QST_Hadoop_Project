# Round 1 介绍
方案介绍：
    在Map中利用正则表达式得到每一行的ip，访问地址后，将其放入set集合中去重。reduce中统计ip即为UV，通过sort排序可得到TOP10。之后保存reduce输出的数据供第二天查询。
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
