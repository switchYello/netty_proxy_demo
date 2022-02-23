package com.proxy.forwarder;

import com.dns.AsnycDns;
import com.handlers.TimeoutHandler;
import com.handlers.TransferHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class ForwarderService extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(ForwarderService.class);

    private String remoteHost;
    private int remotePort;

    public ForwarderService(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    //尝试连接服务器。连接成功后进行读取
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        ChannelFuture promise = createPromise(InetSocketAddress.createUnresolved(remoteHost, remotePort), ctx);
        promise.addListener((ChannelFutureListener) future -> {
            Channel remoteConnection = future.channel();
            if (future.isSuccess() && ctx.channel().isOpen()) {
                log.debug("Forwarder客户端请求连接到服务器 {}:{}", remoteHost, remotePort);
                ctx.pipeline().replace(ctx.name(), null, new TransferHandler(remoteConnection));
                ctx.channel().config().setAutoRead(true);
            } else {
                log.debug("Forwarder连接服务器失败:", future.cause());
                ctx.close();
                remoteConnection.close();
            }
        });
    }

    private ChannelFuture createPromise(final InetSocketAddress address, final ChannelHandlerContext ctx) {
        Bootstrap b = new Bootstrap();
        return b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .resolver(AsnycDns.INSTANCE)
                .remoteAddress(address)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new TimeoutHandler(30, 30, 0));
                        p.addLast(new LoggingHandler("Forwarder服务器连接流"));
                        p.addLast(new TransferHandler(ctx.channel()));
                    }
                })
                .connect();
    }

}
