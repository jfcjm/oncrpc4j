package org.dcache.xdr;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractReplyQueue;
import org.dcache.xdr.model.root.AbstractRpcProtocolFilter;
import org.dcache.xdr.model.root.AbstractRpcReply;
import org.glassfish.grizzly.Connection;

public class GenRpcProtocolFilter extends AbstractRpcProtocolFilter<GenOncRpcSvc,GenOncRpcCall,XdrTransport,GenOncRpcReply>{

    public GenRpcProtocolFilter(ReplyQueueItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> replyQueue) {
        super(replyQueue);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected AbstractRpcReply<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> createRpcReply(Xdr xdr,
            HeaderItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> header,
            XdrTransportItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> transport)
            throws OncRpcException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected XdrTransportItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> createTransport(
            Connection connection, InetSocketAddress address,
            ReplyQueueItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> _replyQueue2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected RpcCallItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> createRpcCall(
            HeaderItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> header, Xdr xdr,
            XdrTransportItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> transport) {
        // TODO Auto-generated method stub
        return  new GenOncRpcCall(header,xdr,transport);
    }

}
