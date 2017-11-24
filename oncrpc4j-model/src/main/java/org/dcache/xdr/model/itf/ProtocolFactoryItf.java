package org.dcache.xdr.model.itf;

import java.io.IOException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.Xdr;

public interface ProtocolFactoryItf<SVC_T extends RpcSvcItf<SVC_T>> {
    
    HeaderItf<SVC_T> decode(Xdr _xdr) throws OncRpcException, IOException;
    
    void preStopActions(RpcSvcItf<SVC_T> rpcSvcItf) throws IOException;
    
    void doPreStartAction(RpcSvcItf<SVC_T> rpcSvcItf) throws IOException;

    void processBuilder(OncRpcSvcBuilderItf<SVC_T> builder);
}
