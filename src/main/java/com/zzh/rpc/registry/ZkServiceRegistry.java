package com.zzh.rpc.registry;

import com.zzh.rpc.config.Constant;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ZkServiceRegistry implements ServiceRegistry
{
    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistry.class);

    private ZkClient zkClient;

    @Value("${rpc.zkAddress}")
    private String zkAddress;

    @PostConstruct
    public void init ()
    {
        zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        logger.info("connect to zookeeper");
    }

    @Override
    public void register (String serviceName, String serviceAddress)
    {
        String rootPath = Constant.ZK_REGISTRY_PATH;
        if(!zkClient.exists(rootPath))
        {
            zkClient.createPersistent(rootPath);
            logger.debug("create registry node: {}", rootPath);
        }
        String servicePath = rootPath + "/" + serviceName;
        if(!zkClient.exists(servicePath))
        {
            zkClient.createPersistent(servicePath);
            logger.debug("create service node: {}", rootPath);
        }
        String addressPath = servicePath + "/address-";
        String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
        logger.debug("create address node: {}", addressNode);
    }
}
