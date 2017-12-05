package org.dcache.xdr;

import java.net.InetAddress;

public class OncRpcClient extends AbstractOncRpcClient<IOncRpcSvc> implements IOncRpcClient{

    public OncRpcClient(InetAddress byName, int udp, int portmapPort) {
        super(byName,udp,portmapPort);
    }

}
