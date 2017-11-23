package org.dcache.xdr;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.dcache.xdr.impl.OncRpcSvcImpl;
import org.dcache.xdr.model.itf.ImplementationGetterItf;
import org.dcache.xdr.model.itf.RpcSvcItf;

public interface OncRpcSvc extends RpcSvcItf<OncRpcSvc>,  ImplementationGetterItf{

    static OncRpcSvc getImpl(IOncRpcSvcBuilder builder) {
        return new OncRpcSvcImpl(builder);
    }
    @Override
    XdrTransport connect(InetSocketAddress socketAddress) throws IOException;
    
}
