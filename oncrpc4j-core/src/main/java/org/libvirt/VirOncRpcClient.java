package org.libvirt;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcClient;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcSvcBuilder;


/**
 * 
 * AU contraire de OncRpcClient, les constructeurs ne nécessitent pas l'utilisation
 * du paramétre prtocole puisque le seulprotocole utilisé par libvirt est TCP.
 * @author jmk
 *
 */

public class VirOncRpcClient extends OncRpcClient {
    final static int libvirtTransportProtocol = IpProtocolType.TCP;
    
    
    public VirOncRpcClient(InetAddress address, int port) {
        this(address,port,0,IoStrategy.SAME_THREAD,null);
    }
    public VirOncRpcClient(InetAddress host,  int port, int localPort, IoStrategy ioStrategy, String serviceName) {
       super(host,libvirtTransportProtocol,port,localPort,ioStrategy,serviceName);
    }
    protected  OncRpcSvcBuilder getOncRpcSvcBuilder() {
         return new VirOncRpcSvcBuilder();
    }
    public synchronized void setPacketWrapper(SASLPacketWrapper sc) throws OncRpcException {
        ((VirOncRpcSvc) _rpcsvc).setPacketWrapper(sc);
    }
}
