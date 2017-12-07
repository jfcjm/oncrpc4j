package org.dcache.xdr;

import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;

import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractGrizzlyXdrTransport;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainContext;

public class GrizzlyXdrTransport extends AbstractGrizzlyXdrTransport<OncRpcSvc, RpcCall, XdrTransport, RpcReply>
        implements XdrTransport {

    public GrizzlyXdrTransport(Connection<InetSocketAddress> connection, InetSocketAddress remoteAddress,
            ReplyQueueItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> replyQueue) {
        super(connection, remoteAddress, replyQueue);
    }

    public GrizzlyXdrTransport(Connection<InetSocketAddress> connection,
            ReplyQueueItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> replyQueue) {
        super(connection,replyQueue);
    }

    @Override
    public XdrTransport getThis() {
        return this;
    }

    @Override
    protected AbstractGrizzlyXdrTransport<OncRpcSvc, RpcCall, XdrTransport, RpcReply> createGrizzlyXdrTransport(
            Connection<InetSocketAddress> connection, ReplyQueueItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> replyQueue) {
        return new GrizzlyXdrTransport(connection,replyQueue);
    }

}
