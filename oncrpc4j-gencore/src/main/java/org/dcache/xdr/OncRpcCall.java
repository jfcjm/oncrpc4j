package org.dcache.xdr;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcCall;

public class OncRpcCall extends AbstractRpcCall<IOncRpcSvc, IOncRpcCall> implements IOncRpcCall {


    public OncRpcCall(int i, int j, RpcAuth auth, XdrTransportItf<IOncRpcSvc, IOncRpcCall> transport) {
        super(i,j,auth,transport);
    }

    public OncRpcCall(HeaderItf<IOncRpcSvc,IOncRpcCall > header, Xdr _xdr, XdrTransportItf<IOncRpcSvc, IOncRpcCall> transport) {
        super(header,_xdr,transport);
    }

}
