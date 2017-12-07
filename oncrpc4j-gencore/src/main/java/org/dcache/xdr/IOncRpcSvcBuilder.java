package org.dcache.xdr;

import org.dcache.xdr.gss.GssSessionManager;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;

public interface IOncRpcSvcBuilder  extends OncRpcSvcBuilderItf
    <
        GenOncRpcSvc,
        GenOncRpcCall,
        IOncRpcSvcBuilder,
        XdrTransport,
        GenOncReply
    > {
    IOncRpcSvcBuilder withGssSessionManager(GssSessionManager gssSessionManager);
    GssSessionManager getGssSessionManager();
    

    IOncRpcSvcBuilder withAutoPublish();
    IOncRpcSvcBuilder withoutAutoPublish();
    
    IOncRpcSvcBuilder withIpProtocolType(int protocol);
    IOncRpcSvcBuilder withTCP();
    IOncRpcSvcBuilder withUDP();
    
    boolean isAutoPublish();
}
