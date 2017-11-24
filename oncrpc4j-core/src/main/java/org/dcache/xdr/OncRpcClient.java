package org.dcache.xdr;

import java.net.InetAddress;

import org.dcache.xdr.model.root.AbstractOncRpcClient;

public class OncRpcClient extends AbstractOncRpcClient<IOncRpcSvc> implements IOncRpcClient{

    public OncRpcClient(InetAddress byName, int udp, int portmapPort) {
        super(byName,udp,portmapPort);
    }

}
