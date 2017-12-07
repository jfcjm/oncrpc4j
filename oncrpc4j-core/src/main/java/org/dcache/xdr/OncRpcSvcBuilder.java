package org.dcache.xdr;

import org.dcache.xdr.gss.GssSessionManager;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;

public class OncRpcSvcBuilder extends AbstractOncRpcSvcBuilder<OncRpcSvc, RpcCall,OncRpcSvcBuilder>  
     {
    
    GssSessionManager _gssSessionManager =null;
    private int _protocol;
    public OncRpcSvcBuilder withGssSessionManager(GssSessionManager gssSessionManager) {
        _gssSessionManager = gssSessionManager;
        return this;
    }

    
    public GssSessionManager getGssSessionManager() {
        return _gssSessionManager;
    }

    @Override
    protected OncRpcSvcBuilder getThis() {
        return this;
    }

    @Override
    protected OncRpcSvc getOncRpcSvc(OncRpcSvcBuilder builder_T) {
        return new OncRpcSvc(this);
    }

    @Override
    public OncRpcSvcBuilder withIpProtocolType(int protocol) {
        super.withIpProtocolType(protocol);
        return this;
    }
}
