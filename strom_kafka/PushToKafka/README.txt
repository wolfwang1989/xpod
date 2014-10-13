
一、目录介绍
	./src				源代码
	pom.xml 			maven工程文件
	pull.config			配置文件
	start.sh			启动脚本
	log4j.properties	日志配置文件
二、编译打包
	1、执行 maven assembly,在target目录下，会生成jar包：PushToKafka-1.0-SNAPSHOT-jar-with-dependencies.jar
	2、将jar包、start.sh 、pull.config 、log4j.properties 打包
三、安装
	1、解压安装包，到安装目录
	2、修改配置，pull.config文件
四、运行
	sh start.sh
