package com.flash.server;

import com.flash.server.handler.FirstServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.util.Date;

/**
 * @ClassName: NettyServer
 * @Description: 服务端
 * @Author: wwangyb
 * @Date: 2020-01-06 22:20
 * @Version: 1.0.0
 */
public class NettyServer {

    private static final int PORT = 8000;

    public static void main(String[] args) {
        //监听端口，accpet新连接的线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        //每一条连接的数据读写的线程组
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        //创建引导类
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        //配置两大线程组
        serverBootstrap.group(bossGroup,workerGroup);
        //指定服务端的IO模型为NIO
        serverBootstrap.channel(NioServerSocketChannel.class);
        //数据的读写处理逻辑
        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel ch){
                ch.pipeline().addLast(new FirstServerHandler());
            }
        });
        //在服务端启动过程中的处理逻辑
        serverBootstrap.handler(new ChannelInitializer<NioServerSocketChannel>() {
            protected void initChannel(NioServerSocketChannel ch) {
                //在服务端启动过程中的处理逻辑
                System.out.println("服务端启动中");
            }
        });
        serverBootstrap.attr(AttributeKey.newInstance("serverName"),"nettyServer");

        //给每一条连接指定自定义属性
        serverBootstrap.childAttr(AttributeKey.newInstance("clientKey"),"clientValue");

        //给每条连接设置TCP底层相关的属性
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE,true);//开启TCP底层心跳机制
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY,true);//开启Nagle算法

        //给服务端channel设置一些属性
        serverBootstrap.option(ChannelOption.SO_BACKLOG,1024);//系统用于临时存放已完成三次握手的请求的队列的最大长度
        bind(serverBootstrap,PORT);
    }

    private static void bind(final ServerBootstrap serverBootstrap,final int port){
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()){
                System.out.println(new Date() + "端口[" + port + "]绑定成功");
            }else{
                System.out.println("端口[" + port + "]绑定失败");
            }
        });
    }
}
