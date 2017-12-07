package org.dcache.xdr;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcCall;

public class GenOncRpcCall extends AbstractRpcCall<
    GenOncRpcSvc, GenOncRpcCall, XdrTransport,GenOncRpcReply
    > implements RpcCallItf<GenOncRpcSvc,GenOncRpcCall,XdrTransport,GenOncRpcReply>{

    public GenOncRpcCall(AbstractRpcCall<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> call) {
        super(call);
    }

    public GenOncRpcCall(HeaderItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> header, Xdr xdr,
            XdrTransportItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> transport) {
        super(header,xdr,transport);
    }

    public GenOncRpcCall(int i, int j, RpcAuth auth, XdrTransport transport) {
        super(i,j,auth,transport);
    }


}
