package org.dcache.xdr;

import java.io.IOException;
import java.net.InetAddress;

import org.dcache.xdr.model.itf.ImplementationGetterItf;
import org.dcache.xdr.model.itf.OncRpcClientItf;

public interface IOncRpcClient extends OncRpcClientItf<OncRpcSvc>,  ImplementationGetterItf{
    static IOncRpcClient getImpl(InetAddress host, int protocol, int port, int localPort, IoStrategy ioStrategy,
            String serviceName) {
        return new OncRpcClient(host,protocol,port,localPort,ioStrategy,serviceName);
    }

    static IOncRpcClient getImpl(InetAddress byName, int ipTransport, int portmapPort) {
        return new OncRpcClient(byName,ipTransport,portmapPort);
    }
    @Override
    XdrTransport connect() throws IOException;
    
}
