package org.dcache.xdr;

import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;

import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractGrizzlyXdrTransport;
import org.glassfish.grizzly.Connection;

public class GrizzlyXdrTransport extends AbstractGrizzlyXdrTransport<GenOncRpcSvc,GenOncRpcCall,XdrTransport,GenOncRpcReply> 
    implements XdrTransport {

    public GrizzlyXdrTransport(Connection<InetSocketAddress> connection, InetSocketAddress remoteAddress,
            ReplyQueueItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> replyQueue) {
        super(connection, remoteAddress, replyQueue);
    }

    public GrizzlyXdrTransport(Connection<InetSocketAddress> _connection2,
            ReplyQueueItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> replyQueueItf) {
        super(_connection2,replyQueueItf);
    }

    @Override
    public XdrTransport getThis() {
        return this;
    }

    @Override
    protected AbstractGrizzlyXdrTransport<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> createGrizzlyXdrTransport(
            Connection<InetSocketAddress> _connection2,
            ReplyQueueItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> replyQueueItf) {
        // TODO Auto-generated method stub
        return new GrizzlyXdrTransport(_connection2,replyQueueItf);
    }


}
