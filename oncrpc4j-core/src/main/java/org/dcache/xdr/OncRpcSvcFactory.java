package org.dcache.xdr;

import java.net.InetAddress;

import org.dcache.xdr.model.itf.OncRpcClientItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public abstract class OncRpcSvcFactory<SVC_T extends RpcSvcItf<SVC_T>> {

    public abstract OncRpcClientItf<SVC_T> createRpcClient(InetAddress address, int tcp, int port) ;
    public abstract RpcCallItf<OncRpcSvc> createRpcCall(int i, int j, RpcAuth auth, XdrTransportItf<SVC_T> transport);

}
