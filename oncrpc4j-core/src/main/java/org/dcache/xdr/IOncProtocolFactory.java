package org.dcache.xdr;

import java.io.IOException;

import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.ProtocolFactoryItf;
import org.dcache.xdr.model.itf.RpcSvcItf;

public interface IOncProtocolFactory extends ProtocolFactoryItf<IOncRpcSvc> {
    
    default void processBuilder(OncRpcSvcBuilderItf<IOncRpcSvc> builder){
        processBuilder((OncRpcSvcBuilder) builder);
    }
    void processBuilder(OncRpcSvcBuilder builder);
    
    default void doPreStartAction(RpcSvcItf<IOncRpcSvc> svc) throws IOException {
        doPreStartAction((IOncRpcSvc) svc);
    }
    void doPreStartAction(IOncRpcSvc svc) throws IOException;
}
