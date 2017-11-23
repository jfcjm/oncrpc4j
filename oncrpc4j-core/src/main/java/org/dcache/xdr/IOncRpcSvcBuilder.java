package org.dcache.xdr;
import org.dcache.xdr.gss.GssSessionManager;
import org.dcache.xdr.model.itf.ImplementationGetterItf;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;

public interface IOncRpcSvcBuilder extends OncRpcSvcBuilderItf<OncRpcSvc>,  ImplementationGetterItf{

    static IOncRpcSvcBuilder getImpl() {
        return new OncRpcSvcBuilder();
    }
    
    IOncRpcSvcBuilder withTCP();
    IOncRpcSvcBuilder withUDP();
    IOncRpcSvcBuilder withIpProtocolType(int protocol);
    
    boolean isAutoPublish();
    IOncRpcSvcBuilder withAutoPublish();
    IOncRpcSvcBuilder withoutAutoPublish();
    GssSessionManager getGssSessionManager();
    IOncRpcSvcBuilder withGssSessionManager(GssSessionManager gssSessionManager);
    
}
