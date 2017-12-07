package org.dcache.xdr;

import java.io.IOException;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractReplyQueue;
import org.dcache.xdr.model.root.AbstractRpcProtocolFilter;

public class RpcProtocolFilter extends AbstractRpcProtocolFilter<OncRpcSvc,RpcCall>{

    public RpcProtocolFilter(ReplyQueueItf<OncRpcSvc, RpcCall> replyQueue) {
        super(replyQueue);
    }

    @Override
    protected RpcCallItf<OncRpcSvc, RpcCall> createRpcCall(HeaderItf<OncRpcSvc, RpcCall> header, Xdr xdr,
            XdrTransportItf<OncRpcSvc, RpcCall> transport) {
        return new RpcCall(header,xdr,transport);
    }

}
