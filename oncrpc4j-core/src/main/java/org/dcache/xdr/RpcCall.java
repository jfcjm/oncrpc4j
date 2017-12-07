package org.dcache.xdr;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.root.AbstractRpcCall;

public class RpcCall extends AbstractRpcCall<OncRpcSvc, RpcCall,XdrTransport,RpcReply> {


    public RpcCall(int i, int j, RpcAuth auth,XdrTransport transport) {
        super(i,j,auth,transport);
    }
    
    public RpcCall(HeaderItf<OncRpcSvc,RpcCall, XdrTransport,RpcReply> header, Xdr _xdr, XdrTransport transport) {
        super(header,_xdr,transport);
    }

    public RpcCall(RpcCall call) {
        super(call);
    }

}
