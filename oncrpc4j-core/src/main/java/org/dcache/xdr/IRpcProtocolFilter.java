package org.dcache.xdr;

import org.dcache.xdr.model.itf.ImplementationGetterItf;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.RpcProtocolFilterUtf;

public interface IRpcProtocolFilter extends RpcProtocolFilterUtf<OncRpcSvc>,  ImplementationGetterItf{

    static IRpcProtocolFilter getImpl( ReplyQueueItf<OncRpcSvc> replyQueue) {
        return new RpcProtocolFilter(replyQueue);
    }

}
