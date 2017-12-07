package org.dcache.xdr;
import java.io.IOException;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcReply;

public class RpcReply extends AbstractRpcReply<OncRpcSvc, RpcCall, XdrTransport,RpcReply> {

    public RpcReply(HeaderItf<OncRpcSvc, RpcCall, XdrTransport,RpcReply> header, Xdr xdr,
            XdrTransportItf<OncRpcSvc, RpcCall, XdrTransport,RpcReply> transport) throws OncRpcException, IOException {
        super(header, xdr, transport);
    }
    public AbstractRpcReply<OncRpcSvc, RpcCall, XdrTransport,RpcReply> lift(){
        return this;
    }
    @Override
    public RpcReply getThis() {
        return this;
    }
}
