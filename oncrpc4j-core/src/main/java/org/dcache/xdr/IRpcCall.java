package org.dcache.xdr;

import org.dcache.xdr.model.itf.ImplementationGetterItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public interface IRpcCall extends RpcCallItf<OncRpcSvc>,  ImplementationGetterItf{

    static IRpcCall getImpl(int prognum, int progver, RpcAuthTypeNone auth, XdrTransportItf<OncRpcSvc> t) {
        // TODO Auto-generated method stub
        return  new RpcCall(prognum,progver,auth,t);
    }

    static IRpcCall getImpl(int i, int j, RpcAuth auth, XdrTransportItf<OncRpcSvc> transport) {
        return new RpcCall(i,j,auth,transport);        
    }

    static IRpcCall getImpl(int i, Xdr xdr, XdrTransport transport) {
        return new RpcCall(i,xdr,transport);
    }

}
