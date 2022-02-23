package com.start;

import com.config.ForwardConfig;
import com.proxy.forwarder.ForwarderInitializer;
import com.utils.SuccessFutureListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hcy
 * @since 2022/2/23 16:26
 */
public class StartForwardProxy {

    private static Logger log = LoggerFactory.getLogger(StartForwardProxy.class);

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workGroup = new NioEventLoopGroup(1);

    private static Options OPTIONS = new Options();


    public static void main(String[] args) {
        OPTIONS.addOption(Option.builder("f").required().hasArg(true).type(String.class).desc("the host of from").build());
        OPTIONS.addOption(Option.builder("t").required().hasArg(true).type(String.class).desc("the host of to").build());
        try {
            CommandLine parse = new DefaultParser().parse(OPTIONS, args);
            String[] fs = parse.getOptionValues("f");
            String[] ts = parse.getOptionValues("t");
            for (int i = 0; i < fs.length && i < ts.length; i++) {
                String[] f = fs[i].split(":");
                String[] t = ts[i].split(":");
                ForwardConfig config = new ForwardConfig();
                config.setFromHost(f[0]);
                config.setFromPort(Integer.parseInt(f[1]));
                config.setToHost(t[0]);
                config.setToPort(Integer.parseInt(t[1]));
                startForwardProxy(config);
            }
        } catch (ParseException e) {
            System.exit(0);
        }
    }

    static void startForwardProxy(ForwardConfig config) {
        log.info("==> {}", config);
        String fromHost = config.getFromHost();
        int fromPort = config.getFromPort();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(new ForwarderInitializer(config));
        ChannelFuture f = b.bind(fromHost, fromPort);
        f.channel().closeFuture().addListener(new SuccessFutureListener<Void>() {
            @Override
            public void operationComplete0(Void future) {
                log.info("service server close");
            }
        });
    }

}
