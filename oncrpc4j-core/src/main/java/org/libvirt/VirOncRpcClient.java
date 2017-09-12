package org.libvirt;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.OncRpcClient;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcSvcBuilder;

public class VirOncRpcClient extends OncRpcClient {
    public VirOncRpcClient(InetAddress host, int protocol, int port, int localPort, IoStrategy ioStrategy) {
       super(host,protocol,port,localPort,ioStrategy);
    }
    protected  OncRpcSvcBuilder getOncRpcSvcBuilder() {
         return new VirOncRpcSvcBuilder();
    }
    public synchronized void setPacketWrapper(SASLPacketWrapper sc) throws OncRpcException {
        ((VirOncRpcSvc) _rpcsvc).setPacketWrapper(sc);
    }
}
