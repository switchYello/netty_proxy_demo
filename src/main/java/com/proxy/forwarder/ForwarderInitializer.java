package com.proxy.forwarder;

import com.handlers.TimeoutHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

/*
 * 将连接全部转发到另外一个服务器:端口上
 * 这里硬编码www.bilibili.com:80做示例
 * 当然bilibili那边是不会处理我们的请求的，会返回一个403
 */
public class ForwarderInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new TimeoutHandler(30, 30, 0));
        p.addLast(new LoggingHandler("Forwarder客户端请求流"));
        p.addLast(new ForwarderService("www.bilibili.com", 80));
    }


}
