package com.zzh.rpc.serialize.impl;

import com.zzh.rpc.serialize.Serializer;
import com.zzh.rpc.serialize.SerializerAlforithm;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtostuffSerializer implements Serializer
{
    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    @Override
    public byte getSerializerAlgorithm ()
    {
        return SerializerAlforithm.protostuff;
    }

    @Override
    public <T> byte[] serialize (T obj)
    {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try
        {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e)
        {
            throw new IllegalStateException(e.getMessage(), e);
        } finally
        {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialize (Class<T> cls, byte[] data)
    {
        try
        {
            T message = cls.newInstance();
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e)
        {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Schema<T> getSchema (Class<T> cls)
    {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if(schema == null)
        {
            schema = RuntimeSchema.createFrom(cls);
            cachedSchema.put(cls, schema);
        }
        return schema;
    }
}
