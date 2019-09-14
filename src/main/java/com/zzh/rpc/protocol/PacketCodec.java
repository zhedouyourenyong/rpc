package com.zzh.rpc.protocol;

import com.zzh.rpc.protocol.request.RpcRequest;
import com.zzh.rpc.protocol.response.RpcResponse;
import com.zzh.rpc.serialize.Serializer;
import com.zzh.rpc.serialize.impl.ProtostuffSerializer;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class PacketCodec
{
    public static final PacketCodec INSTANCE = new PacketCodec();

    public static final int MAGIC_NUMBER = 0x16130505;
    private final Map<Byte, Class<?>> packetTypeMap;
    private final Map<Byte, Serializer> serializerMap;


    private PacketCodec ()
    {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(Command.RPC_REQUEST, RpcRequest.class);
        packetTypeMap.put(Command.RPC_RESPONSE, RpcResponse.class);

        serializerMap = new HashMap<>();
        Serializer serializer = new ProtostuffSerializer();
        serializerMap.put(serializer.getSerializerAlgorithm(), serializer);
    }

    public static void encode (ByteBuf buf, Packet packet)
    {
        byte[] bytes = Serializer.DEFAULT.serialize(packet);

        buf.writeInt(MAGIC_NUMBER);
        buf.writeByte(packet.getVersion());
        buf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm());
        buf.writeByte(packet.getCommand());
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public<T> T decode (ByteBuf byteBuf)
    {
        // 跳过 magic number
        byteBuf.skipBytes(4);

        // 跳过版本号
        byteBuf.skipBytes(1);

        // 序列化算法
        byte serializeAlgorithm = byteBuf.readByte();

        // 指令
        byte command = byteBuf.readByte();

        // 数据包长度
        int length = byteBuf.readInt();

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        Class<T> requestType = (Class<T>) getRequestType(command);
        Serializer serializer = getSerializer(serializeAlgorithm);

        if(requestType != null && serializer != null)
        {
            return serializer.deserialize(requestType, bytes);
        }
        return null;
    }

    private Serializer getSerializer (byte serializeAlgorithm)
    {

        return serializerMap.get(serializeAlgorithm);
    }

    private Class<?> getRequestType (byte command)
    {

        return packetTypeMap.get(command);
    }
}
