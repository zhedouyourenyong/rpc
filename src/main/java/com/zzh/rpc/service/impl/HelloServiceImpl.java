package com.zzh.rpc.service.impl;

import com.zzh.rpc.server.RpcService;
import com.zzh.rpc.service.HelloService;

@RpcService(HelloServiceImpl.class)
public class HelloServiceImpl implements HelloService
{
    @Override
    public String say (String name)
    {
        return "hello " + name;
    }
}