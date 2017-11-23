package org.libvirt;

import java.net.InetAddress;

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.model.itf.OncRpcClientItf;

public interface IVirRpcClient extends OncRpcClientItf<VirRpcSvc>{
    public static IVirRpcClient getImpl(InetAddress address, int port){
        return new VirRpcClient(address, port) ;
    }

    public static IVirRpcClient getImpl(InetAddress host, int protocol, int port, int localPort,
            IoStrategy ioStrategy, String serviceName) {
        return new VirRpcClient(host, protocol,port, localPort, ioStrategy, serviceName);
    }

    public static IVirRpcClient getImpl(InetAddress host,  int port, int localPort, IoStrategy ioStrategy, String serviceName){
        return new VirRpcClient(host, port, localPort, ioStrategy, serviceName);
    }
    
    
    void setPacketWrapper(SASLPacketWrapper sc) throws OncRpcException;

}