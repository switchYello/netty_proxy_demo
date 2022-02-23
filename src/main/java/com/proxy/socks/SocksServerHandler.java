package com.proxy.socks;

import com.dns.AsnycDns;
import com.handlers.TransferHandler;
import com.utils.ChannelUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import io.netty.handler.codec.socksx.v4.Socks4CommandType;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@ChannelHandler.Sharable
public class SocksServerHandler extends SimpleChannelInboundHandler<SocksMessage> {

    public static SocksServerHandler INSTANCE = new SocksServerHandler();

    private static Logger log = LoggerFactory.getLogger(SocksServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksMessage msg) {
        if (msg.decoderResult() != DecoderResult.SUCCESS) {
            ctx.fireExceptionCaught(msg.decoderResult().cause());
            return;
        }
        switch (msg.version()) {
            case SOCKS5:
                //客户端将支持的所有加密方式发过来，服务端选择一种加密方式回复，并在前面添加处理类。我这里直接回复无需验证
                if (msg instanceof Socks5InitialRequest) {
                    ctx.pipeline().addBefore(ctx.name(), null, new Socks5CommandRequestDecoder());
                    ctx.writeAndFlush(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
                }
                //如果上一步选择了需要验证，则此处接收到客户端的验证信息。但是chrome不支持验证的方式
                else if (msg instanceof Socks5PasswordAuthRequest) {
                    ctx.writeAndFlush(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS));
                }
                //判断连接是否是CONNECT，并将数据传到下一个handler处理
                else if (msg instanceof Socks5CommandRequest) {
                    Socks5CommandRequest socks5CmdRequest = (Socks5CommandRequest) msg;
                    if (socks5CmdRequest.type() == Socks5CommandType.CONNECT) {
                        socks5LocalToRemote(ctx, socks5CmdRequest);
                    } else {
                        ctx.close();
                    }
                }
                break;
            case SOCKS4a:
                Socks4CommandRequest socksV4CmdRequest = (Socks4CommandRequest) msg;
                if (socksV4CmdRequest.type() == Socks4CommandType.CONNECT) {
                    socks4LocalToRemote(ctx, socksV4CmdRequest);
                } else {
                    ctx.close();
                }
                break;
            default:
                ctx.close();
                break;
        }
    }

    private static void socks5LocalToRemote(ChannelHandlerContext ctx, Socks5CommandRequest request) {
        String host = request.dstAddr();
        int port = request.dstPort();
        Socks5AddressType type = request.dstAddrType();
        Bootstrap b = new Bootstrap();
        b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .resolver(AsnycDns.INSTANCE)
                .remoteAddress(host, port)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new LoggingHandler("socks5网站链接流"))
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        Channel remoteChannel = future.channel();
                        if (future.isSuccess() && ctx.channel().isOpen()) {
                            remoteChannel.pipeline().addLast(new TransferHandler(ctx.channel()));
                            ctx.pipeline().replace(ctx.name(), null, new TransferHandler(remoteChannel));
                            ctx.writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, type, host, port));
                            return;
                        }
                        remoteChannel.close();
                        ctx.writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, type, host, port));
                        ctx.close();
                    }
                });
    }

    private static void socks4LocalToRemote(ChannelHandlerContext ctx, Socks4CommandRequest msg) {
        String host = msg.dstAddr();
        int port = msg.dstPort();
        Bootstrap b = new Bootstrap();
        b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .remoteAddress(host, port)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new LoggingHandler("socks4网站链接流"))
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        Channel remoteChannel = future.channel();
                        if (future.isSuccess() && ctx.channel().isOpen()) {
                            remoteChannel.pipeline().addLast(new TransferHandler(ctx.channel()));
                            ctx.pipeline().replace(ctx.name(), null, new TransferHandler(remoteChannel));
                            ctx.writeAndFlush(new DefaultSocks4CommandResponse(Socks4CommandStatus.SUCCESS, host, port));
                            return;
                        }
                        remoteChannel.close();
                        ctx.writeAndFlush(new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED, host, port));
                        ctx.close();
                    }
                });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("SocksServerHandler", cause);
        ChannelUtil.closeOnFlush(ctx.channel());
    }

}
