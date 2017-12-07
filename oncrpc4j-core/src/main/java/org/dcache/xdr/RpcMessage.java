package org.dcache.xdr;

import java.io.IOException;

import org.dcache.xdr.model.root.AbstractRpcMessage;

public class RpcMessage extends AbstractRpcMessage<OncRpcSvc, RpcCall,XdrTransport,RpcReply> {

    public RpcMessage(int xid, int callType) {
        super(xid,callType);
    }

    public RpcMessage(Xdr xdr) throws OncRpcException, IOException {
        super(xdr);
    }

}
