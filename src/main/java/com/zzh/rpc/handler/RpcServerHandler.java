package com.zzh.rpc.handler;

import com.zzh.rpc.protocol.request.RpcRequest;
import com.zzh.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest>
{
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

    private Map<String, Object> handlerMap;

    public RpcServerHandler (Map<String, Object> handlerMap)
    {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0 (ChannelHandlerContext ctx, RpcRequest request) throws Exception
    {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        try
        {
            Object result = handle(request);
            response.setResult(result);
        } catch (Exception e)
        {
            response.setException(e);
            logger.error("handle result failure", e);
        }
        // 写入完毕后立即关闭与客户端的连接
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle (RpcRequest request) throws Exception
    {
        String serviceName = request.getInterfaceName();
        Object serviceBean = handlerMap.get(serviceName);
        if(serviceBean == null)
        {
            throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught (ChannelHandlerContext ctx, Throwable cause)
    {
        logger.error("server caught exception", cause);
        ctx.close();
    }
}
