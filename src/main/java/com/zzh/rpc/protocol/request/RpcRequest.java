package com.zzh.rpc.protocol.request;

import com.zzh.rpc.protocol.Command;
import com.zzh.rpc.protocol.Packet;
import lombok.Data;

@Data
public class RpcRequest extends Packet
{
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] parameterTypes;

    @Override
    public Byte getCommand ()
    {
        return Command.RPC_REQUEST;
    }
}
