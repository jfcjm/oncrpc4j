package org.dcache.xdr;
import java.io.IOException;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcReply;

public class GenRpcReply extends AbstractRpcReply<GenOncRpcSvc, GenOncRpcCall, XdrTransport,GenRpcReply> {

    public GenRpcReply(HeaderItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenRpcReply> header, Xdr xdr,
            XdrTransportItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport, GenRpcReply> transport)
            throws OncRpcException, IOException {
        super(header, xdr, transport);
    }

    @Override
    public GenRpcReply getThis() {
        return this;
    }
}
