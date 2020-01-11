package com.flash.client;

import com.flash.client.handler.FirstClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: NettyClient
 * @Description: 客户端
 * @Author: wwangyb
 * @Date: 2020-01-07 21:46
 * @Version: 1.0.0
 */
public class NettyClient {

    private final static int MAX_RETRY = 5;

    public static void main(String[] args) {
        //创建一个线程组
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        //创建引导类
        Bootstrap bootstrap = new Bootstrap();
        //指定线程模型
        bootstrap.group(workerGroup);
        //指定IO类型
        bootstrap.channel(NioSocketChannel.class);
        //给每条连接设置TCP底层相关的属性
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);//连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);//开启TCP底层心跳机制
        bootstrap.option(ChannelOption.TCP_NODELAY, true);//开启Nagle算法
        //IO处理逻辑
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new FirstClientHandler());
            }
        });
        //建立连接
        connect(bootstrap,"127.0.0.1",8000,MAX_RETRY);
    }

    /**
     * 建立连接
     * @param bootstrap
     * @param host
     * @param port
     */
    private static void connect(Bootstrap bootstrap,String host,int port,int retry){
        bootstrap.connect(host,port).addListener(future -> {
            if (future.isSuccess()){
                System.out.println("连接成功");
            }else if (retry == 0){
                System.out.println("重试次数已用完，放弃连接！");
            }else{
                System.out.println("连接失败,开始重连");
                // 第几次重连
                int order = (MAX_RETRY - retry) + 1;
                // 本次重连的间隔
                int delay = 1 << order;
                System.err.println(new Date() + ": 连接失败，第" + order + "次重连……");
                bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit
                        .SECONDS);
            }
        });
    }
}
