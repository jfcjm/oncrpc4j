package org.dcache.xdr;

import org.dcache.xdr.model.root.AbstractRpcMessage;

public class RpcMessage extends AbstractRpcMessage<OncRpcSvc, RpcCall> {

    public RpcMessage(int xid, int callType) {
        super(xid,callType);
    }

}
