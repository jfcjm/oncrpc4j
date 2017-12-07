package org.dcache.xdr;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractGrizzlyXdrTransport;
import org.dcache.xdr.model.root.AbstractRpcProtocolFilter;
import org.dcache.xdr.model.root.AbstractRpcReply;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainContext;

public class RpcProtocolFilter extends AbstractRpcProtocolFilter<OncRpcSvc,RpcCall,XdrTransport, RpcReply>{

    public RpcProtocolFilter(ReplyQueueItf<OncRpcSvc, RpcCall,XdrTransport, RpcReply> replyQueue) {
        super(replyQueue);
    }

    @Override
    protected RpcCallItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> createRpcCall(
            HeaderItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> header, Xdr xdr,
            XdrTransportItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> transport) {
        // TODO Auto-generated method stub
        return  new RpcCall(header,xdr,transport.getThis());
    }

    @Override
    protected XdrTransportItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> createTransport(Connection connection,
            InetSocketAddress address, ReplyQueueItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> replyQueue) {
        // TODO Auto-generated method stub
        return new GrizzlyXdrTransport(connection,address,replyQueue);
    }

    @Override
    protected AbstractRpcReply<OncRpcSvc, RpcCall, XdrTransport, RpcReply> createRpcReply(Xdr xdr,
            HeaderItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> header,
            XdrTransportItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> transport) throws OncRpcException, IOException{
        // TODO Auto-generated method stub
        return new RpcReply(header, xdr, transport);
    }}
