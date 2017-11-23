package org.dcache.xdr;

import java.io.IOException;

import org.dcache.xdr.impl.RpcReplyImpl;
import org.dcache.xdr.model.itf.ImplementationGetterItf;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public interface RpcReply extends RpcReplyItf<OncRpcSvc>,ImplementationGetterItf {
    //TOD Replace with an interface implementing XdrTransport<OncRpcSvc>
    static RpcReply getImpl(int xid, Xdr xdr, XdrTransportItf<OncRpcSvc> transport) throws OncRpcException, IOException {
        return new RpcReplyImpl(xid,xdr,transport);
    }

}
