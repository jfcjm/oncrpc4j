package org.dcache.xdr.model.root;

import java.io.IOException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.RpcProtocolFilterUtf;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract  class AbstractRpcProtocolFilter<SVC_T extends RpcSvcItf<SVC_T>> extends BaseFilter implements RpcProtocolFilterUtf<SVC_T>{

    private final static Logger _log = LoggerFactory.getLogger(AbstractRpcProtocolFilter.class);
    protected final ReplyQueueItf<SVC_T> _replyQueue;

    public AbstractRpcProtocolFilter(ReplyQueueItf<SVC_T> replyQueue) {
       _replyQueue = replyQueue;
    }
    

    protected abstract RpcReplyItf<SVC_T> createReply(Xdr xdr, RpcMessage message, XdrTransportItf<SVC_T> transport)
            throws OncRpcException, IOException;
}
