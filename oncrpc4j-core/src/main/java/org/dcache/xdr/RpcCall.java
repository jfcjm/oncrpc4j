package org.dcache.xdr;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcCall;

public class RpcCall extends AbstractRpcCall<OncRpcSvc, RpcCall> {


    public RpcCall(int i, int j, RpcAuth auth, XdrTransportItf<OncRpcSvc, RpcCall> transport) {
        super(i,j,auth,transport);
    }

    public RpcCall(HeaderItf<OncRpcSvc,RpcCall > header, Xdr _xdr, XdrTransportItf<OncRpcSvc, RpcCall> transport) {
        super(header,_xdr,transport);
    }

}
