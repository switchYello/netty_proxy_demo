package com.start;

import com.proxy.socks.SocksProxyInitializer;
import com.utils.SuccessFutureListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hcy
 * @since 2022/2/23 19:00
 */
public class StartSockProxy {

    private static Logger log = LoggerFactory.getLogger(StartSockProxy.class);

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workGroup = new NioEventLoopGroup(1);

    public static void main(String[] args) {
        startSocksProxy();
    }

    static void startSocksProxy() {
        String localHost = "127.0.0.1";
        int localPort = 1080;

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
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


}
