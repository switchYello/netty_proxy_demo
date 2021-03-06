package com.proxy.socks.socks4;

import com.handlers.TransferHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import io.netty.handler.logging.LoggingHandler;

public class Socks4ServerConnectHandler extends SimpleChannelInboundHandler<Socks4CommandRequest> {

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Socks4CommandRequest msg) throws Exception {
        String host = msg.dstAddr();
        int port = msg.dstPort();
        Bootstrap b = new Bootstrap();
        b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .remoteAddress(host, port)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new LoggingHandler("socks4网站链接流"))
                .connect().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    ctx.writeAndFlush(new DefaultSocks4CommandResponse(Socks4CommandStatus.SUCCESS, msg.dstAddr(), msg.dstPort()));
                    future.channel().pipeline().addLast(new TransferHandler(ctx.channel()));
                    ctx.pipeline().replace(ctx.name(), null, new TransferHandler(future.channel()));
                } else {
                    ctx.writeAndFlush(new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED, msg.dstAddr(), msg.dstPort()));
                    ctx.close();
                }
            }
        });
    }


}
