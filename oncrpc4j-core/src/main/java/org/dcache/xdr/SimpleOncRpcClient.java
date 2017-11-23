package org.dcache.xdr;

import java.net.InetAddress;

import org.dcache.xdr.model.itf.OncRpcClientItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public class SimpleOncRpcClient extends AbstractSimpleRpcClient<OncRpcSvc> {

    @Override
    protected RpcCallItf<OncRpcSvc> createRpcCall(int i, int j, RpcAuth auth, XdrTransportItf<OncRpcSvc> transport) {
        return IRpcCall.getImpl(i, j, auth, transport);
    }

    @Override
    protected OncRpcClientItf<OncRpcSvc> createRpcClient(InetAddress address, int tcp, int port) {
        return IOncRpcClient.getImpl(address,tcp,port);
    }

    @Override
    protected AbstractSimpleRpcClient<OncRpcSvc> createSimpleClient() {
        return this;
    }
    public void main(String[] args) throws Exception {
        new SimpleOncRpcClient().process(args);
    }
}
