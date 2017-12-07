package org.dcache.xdr;
import java.io.IOException;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcReply;

public class GenOncRpcReply extends AbstractRpcReply
    <
        GenOncRpcSvc, GenOncRpcCall, XdrTransport,GenOncRpcReply
    > {

    public GenOncRpcReply(HeaderItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> header, Xdr xdr,
            XdrTransportItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenOncRpcReply> transport)
            throws OncRpcException, IOException {
        super(header, xdr, transport);
    }

    @Override
    public GenOncRpcReply getThis() {
        return this;
    }
}
