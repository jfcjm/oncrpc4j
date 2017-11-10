package org.dcache.xdr.model.root;

import java.io.IOException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.model.itf.GenItfReplyQueue;
import org.dcache.xdr.model.itf.GenItfRpcProtocolFilter;
import org.dcache.xdr.model.itf.GenItfRpcReply;
import org.dcache.xdr.model.itf.GenItfRpcSvc;
import org.dcache.xdr.model.itf.GenItfXdrTransport;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract  class GenAbstractRpcProtocolFilter<SVC_T extends GenItfRpcSvc<SVC_T>> extends BaseFilter implements GenItfRpcProtocolFilter<SVC_T>{

    private final static Logger _log = LoggerFactory.getLogger(GenAbstractRpcProtocolFilter.class);
    protected final GenItfReplyQueue<SVC_T> _replyQueue;

    public GenAbstractRpcProtocolFilter(GenItfReplyQueue<SVC_T> replyQueue) {
       _replyQueue = replyQueue;
    }
    

    protected abstract GenItfRpcReply<SVC_T> createReply(Xdr xdr, RpcMessage message, GenItfXdrTransport<SVC_T> transport)
            throws OncRpcException, IOException;
}
