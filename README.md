本系统运行原理：
1. 根据邮箱地址 和 密码 登录邮箱，过滤含有银行、信用和账单字眼的主题邮件；

2. 绑定邮箱的同时对邮箱添加转发规则，以后每次登录的时候，先判断是否添加了转发规则，若已经添加跳过，否则添加；（目前未生效）

3. 下载存储的路径为：
	3.1 #	存储在DFS上时Nginx配置的此路径
		domainPath=http://yxb1.umpay.com/
		#		DFS分组的组名配置
		group = group1  		【131linux测试环境路径】
		
	3.2	另注，路径规则, 举例说明，绑定的邮箱为zzj99@126.com, 抓取的账单为工商银行的
	    ~邮件拼接路径（分别于DFS的路径相对）：
	    z/z/zzj99@126.com/webmaster@icbc.com.cn/xxx.html(原件)
	    z/z/zzj99@126.com/webmaster@icbc.com.cn/xxx_second.html(截取后的原件)
	   
	    ~DFS存储（group1为DFS的组名；M开始往后为DFS分配的路径）:
    	/group1/M00/4A/0A/CgpJjlQnhReAX9H3AAAivKAEYik69.html(原件)
    	/group1/M00/4A/0A/CgpJjlQnhReAKE1iAAAh3akQYkc78.html(截取后的原件)
    	
    	xxx.html文件的命名的规则为： 主题_收件人邮箱_收件时间
    	/group1/M00/4A/0A/xxx.html命名规则：DFS组名（group1）+DFS自动分配的路径（M00/4A/0A/xxx.html）；
    	
4. 读取截取后xxx_second.html, 使用xsoup将html的所有的标签过滤掉

5. 根据不同的银行调用不同的解析模板

6. 入库表结构化数据

7. 推送到用户的手机端

8. 定时任务每天跑job，查询账单日是今天的的用户全部再读取其邮箱，抓取新账单，若未抓取到新账单，延续5天抓取；


--- 部署131测试环境，检查点：


1. 在141测试数据库   测试库用户名/测试库密码   和 xieyang/umpayoracle 执行最新的数据库脚本oracle.sql；

2. 检查配置文件：system.properties 中 连接数据库的属性是否为    测试库用户名/测试库密码；

3. 检查配置文件：serverHtmlPath.properties 中配置项是否为 domainPath=http://yxb1.umpay.com/；

4. 检查配置文件： system.properties 端口号transport.http.port 是否为8186；


---	对于配置文件的说明

1. bankMailAddress.properties:
	15家银行的账单发件人email地址
2. callMails.properties
	测试频率的一些数据，还有httpFetch通过http抓取的邮箱类型。
3. ehcache-hibernate-local.xml
	关于hibernate的一些配置
4. fdfs_client.conf
	有关DFS的配置文件信息
5. log4j.properties
	关于日志的配置信息
6. orclsql.sql
	sql建表语句
7. queryKey.properties
	7.1 queryKey：过滤关键字的配置
	7.2 forward_key：转发关键字配置
	7.3 rule_name：添加的转发规则名称
	7.4   其他：一些关于某银行的特殊关键字的配置
8. serverHtmlPath.properties
	8.1 serverHtmlPath：暂时无效，项目中用到较多未删
	8.2 domainPath：Nginx配置的路径
	8.3 group：DFS的组名
	8.4 jobDay：日账单扫描最多天数
9. system.properties
	9.1  关于数据库连接的配置
	9.2  关于hibernate配置文件的配置
	9.3  关于多线程线程池的配置
	9.4 项目启动QueuedThreadPool的配置
10. 邮件账单表结构20140912.docx
	账单表结构的明细
11. 有关Websocket的自定义协议.docx
	websocket的自定义协议
	银行卡详细类型返回值的含义
