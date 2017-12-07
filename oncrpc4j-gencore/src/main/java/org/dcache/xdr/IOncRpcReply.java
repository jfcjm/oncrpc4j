package org.dcache.xdr;
import java.io.IOException;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcReply;

public interface IOncRpcReply extends RpcReplyItf<GenOncRpcSvc, GenOncRpcCall, XdrTransport,GenRpcReply> {
}
