package com.zzh.rpc.server;

import com.zzh.rpc.codec.PacketCodecHandler;
import com.zzh.rpc.codec.Spliter;
import com.zzh.rpc.handler.RpcServerHandler;
import com.zzh.rpc.registry.ZkServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Component
public class RpcServer implements ApplicationContextAware, InitializingBean
{
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);
    private Map<String, Object> handlerMap = new HashMap<>();

    @Autowired
    private ZkServiceRegistry zkServiceRegistry;
    @Value("${rpc.port}")
    private int port;

    @Override
    public void setApplicationContext (ApplicationContext ctx) throws BeansException
    {
        Map<String, Object> beanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if(beanMap != null && beanMap.size() != 0)
        {
            for (Object bean : beanMap.values())
            {
                RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
                String serviceName = rpcService.value().getSimpleName();
                handlerMap.put(serviceName, bean);
            }
        }
    }

    @Override
    public void afterPropertiesSet () throws Exception
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try
        {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>()
                    {
                        @Override
                        protected void initChannel (NioSocketChannel channel) throws Exception
                        {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast("spliter", new Spliter());
                            pipeline.addLast("codec", PacketCodecHandler.INSTANCE);
                            pipeline.addLast("handler", new RpcServerHandler(handlerMap));
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("server started, listening on {}", port);

            String serviceAddress = InetAddress.getLocalHost().getHostAddress() + ":" + port;
            for (String interfaceName : handlerMap.keySet())
            {
                zkServiceRegistry.register(interfaceName, serviceAddress);
                logger.info("register service: {} => {}", interfaceName, serviceAddress);
            }
            future.channel().closeFuture().sync();
        } catch (Exception e)
        {
            logger.error("server exception", e);
        } finally
        {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
