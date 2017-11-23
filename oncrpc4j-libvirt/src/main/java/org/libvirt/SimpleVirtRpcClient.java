package org.libvirt;

import java.net.InetAddress;

import org.dcache.xdr.AbstractSimpleRpcClient;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.model.itf.OncRpcClientItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public class SimpleVirtRpcClient extends AbstractSimpleRpcClient<VirRpcSvc> {

    @Override
    protected RpcCallItf<VirRpcSvc> createRpcCall(int i, int j, RpcAuth auth,
            XdrTransportItf<VirRpcSvc> transport) {
        return IVirRpcCall.getImpl(i,j,auth,transport);
    }

    @Override
    protected OncRpcClientItf<VirRpcSvc> createRpcClient(InetAddress address, int tcp, int port) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected AbstractSimpleRpcClient<VirRpcSvc> createSimpleClient() {
        // TODO Auto-generated method stub
        return null;
    }
}
