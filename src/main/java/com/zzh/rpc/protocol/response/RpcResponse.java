package com.zzh.rpc.protocol.response;

import com.zzh.rpc.protocol.Command;
import com.zzh.rpc.protocol.Packet;
import lombok.Data;

@Data
public class RpcResponse extends Packet
{
    private String requestId;
    private Exception exception;
    private Object result;

    boolean hasException()
    {
        return exception!=null;
    }

    @Override
    public Byte getCommand ()
    {
        return Command.RPC_RESPONSE;
    }
}
