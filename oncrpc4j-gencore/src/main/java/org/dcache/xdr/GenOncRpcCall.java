package org.dcache.xdr;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcCall;

public class GenOncRpcCall extends AbstractRpcCall<
    GenOncRpcSvc, GenOncRpcCall, XdrTransport,GenRpcReply
    > implements RpcCallItf<GenOncRpcSvc,GenOncRpcCall,XdrTransport,GenRpcReply>{

    public GenOncRpcCall(AbstractRpcCall<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenRpcReply> call) {
        super(call);
    }


}
