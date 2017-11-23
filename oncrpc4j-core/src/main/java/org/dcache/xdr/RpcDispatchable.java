package org.dcache.xdr;

import java.io.IOException;

import org.dcache.xdr.model.itf.RpcCallItf;

public interface RpcDispatchable extends org.dcache.xdr.model.itf.RpcDispatchable<OncRpcSvc> {
    /**
     * We provide a default implementation to Override org.dcache.xdr.model.itf.RpcDispatchable<OncRpcSvc>#
     * calling an overloading  operation with profile defined for RpcCall.
     */
    @Override
    default public  void dispatchOncRpcCall( RpcCallItf<OncRpcSvc> call)
            throws OncRpcException, IOException  {
        dispatchOncRpcCall((RpcCall ) call);
    }
    void dispatchOncRpcCall(RpcCall call) throws OncRpcException, IOException;
}
