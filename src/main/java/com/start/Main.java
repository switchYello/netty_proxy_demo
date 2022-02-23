package com.start;


import com.proxy.httpProxy.HttpProxyInitializer;
import com.proxy.socks.SocksProxyInitializer;
import com.utils.SuccessFutureListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workGroup = new NioEventLoopGroup(1);


    public static void main(String[] args) {
        testSocks4_5();
    }

    static void testSocks4_5() {
        String localHost = "127.0.0.1";
        int localPort = 1080;

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childOption(ChannelOption.SO_LINGER, 1)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new SocksProxyInitializer());
        ChannelFuture f = b.bind(localHost, localPort);
        f.channel().closeFuture().addListener(new SuccessFutureListener<Void>() {
            @Override
            public void operationComplete0(Void future) {
                log.info("service server close");
            }
        });
    }

    static void testHttpProxy() {
        String localHost = "127.0.0.1";
        int localPort = 1080;

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childOption(ChannelOption.SO_LINGER, 1)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new HttpProxyInitializer());
        ChannelFuture f = b.bind(localHost, localPort);
        f.channel().closeFuture().addListener(new SuccessFutureListener<Void>() {
            @Override
            public void operationComplete0(Void future) {
                log.info("service server close");
            }
        });
    }


}
