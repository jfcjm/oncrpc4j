package org.dcache.xdr;

import java.io.IOException;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractReplyQueue;
import org.dcache.xdr.model.root.AbstractRpcProtocolFilter;

public class RpcProtocolFilter extends AbstractRpcProtocolFilter<IOncRpcSvc,IOncRpcCall>{

    public RpcProtocolFilter(ReplyQueueItf<IOncRpcSvc, IOncRpcCall> replyQueue) {
        super(replyQueue);
    }

    @Override
    protected RpcCallItf<IOncRpcSvc, IOncRpcCall> createRpcCall(HeaderItf<IOncRpcSvc, IOncRpcCall> header, Xdr xdr,
            XdrTransportItf<IOncRpcSvc, IOncRpcCall> transport) {
        return new OncRpcCall(header,xdr,transport);
    }

}
