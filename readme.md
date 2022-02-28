

### 这是使用netty开发的http代理，socks4 socks5代理，hap代理的示例


## 启动类在com.start包下

StartForwardProxy 用于测试tcp转发
>   java -jar demo.jar -f127.0.0.1:53306 -t127.0.0.1:3306，可直接在ide中添加启动参数即可

StartSockProxy 用于测试socks代理
>   默认在本地1080端口监听

Main 用于测试http代理
>   默认在本地1080端口监听