关于application.properties 属性的配置
1、以下必配置项,其它属性必须有但可以不配置
additional.tomcat.connectors.flg=true
tomcat.connector.customizer.flg=true

2、#spring boot tomcat9.0.12 Log ：
部分为spring boot 内置tomcat具体参数需要去spring boot 关网查阅设定。
3、#Tomcat9.0.12 Connection Customizer：
部分为添加的Tomcat连接器具体参数需要去tomcat 9.0.12的官网查阅设定。
然后再修改moxiu.com.server.customizer.ApplicationPropertiesBean类进行解析。

4、#addAdditional Tomcat Connectors
部分为附加的Tomcat连接器，具体参数需要去tomcat 9.0.12的官网查阅设定。
然后再修改moxiu.com.server.customizer.ApplicationPropertiesBean类进行解析。

5、当additional.tomcat.connectors.flg=flase并且tomcat.connector.customizer.flg=flase时,走springboot配置文件 server.port的端口。
当additional.tomcat.connectors.flg=flase并且tomcat.connector.customizer.flg=true时,走tomcat连接器的端口。
当additional.tomcat.connectors.flg=true并且tomcat.connector.customizer.flg=false时,走springboot配置文件 server.port的端口和附加的Tomcat连接器端口。