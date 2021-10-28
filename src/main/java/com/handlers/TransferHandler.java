package com.handlers;

import com.utils.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 交换输入输出
 */
public class TransferHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(TransferHandler.class);
    //读取数据写入这个channel里
    private Channel outChannel;
    //是否是自动读取，如果不是自动读取，则需要写完后手动read
    private boolean autoRead;

    public TransferHandler(Channel outChannel) {
        this(outChannel, true);
    }

    public TransferHandler(Channel outChannel, boolean autoRead) {
        this.outChannel = outChannel;
        this.autoRead = autoRead;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (!autoRead) {
            ctx.read();
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (!outChannel.isActive()) {
            ReferenceCountUtil.release(msg);
        }
        outChannel.writeAndFlush(msg).addListener(future -> {
            if (!future.isSuccess()) {
                ReferenceCountUtil.release(msg);
                ctx.close();
                outChannel.close();
            }
            //不是自动读取，则需要手动读取一次
            if (!autoRead) {
                ctx.read();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelUtil.closeOnFlush(outChannel);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.debug("", cause);
    }
}
