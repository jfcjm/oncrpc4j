package org.dcache.xdr.impl;

import java.net.InetSocketAddress;

import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.model.impl.AbstractGrizzlyXdrTransport;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.glassfish.grizzly.Connection;

public class GrizzlyXdrTransport extends AbstractGrizzlyXdrTransport<OncRpcSvc> implements XdrTransport {

    public GrizzlyXdrTransport(Connection<InetSocketAddress> connection, ReplyQueueItf<OncRpcSvc> replyQueue) {
        super(connection, replyQueue);
    }
    
}
