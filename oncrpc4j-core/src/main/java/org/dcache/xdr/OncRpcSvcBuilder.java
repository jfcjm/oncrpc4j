package org.dcache.xdr;

import org.dcache.xdr.gss.GssSessionManager;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;

public class OncRpcSvcBuilder extends AbstractOncRpcSvcBuilder<IOncRpcSvc> implements IOncRpcSvcBuilder {

    private GssSessionManager _gssSessionManager;
    
    OncRpcSvcBuilder(){
        super(new OncRpcProtocolFactory());
    }
    
    
    @Override
    public GssSessionManager getGssSessionManager() {
        return _gssSessionManager;
    }
    
    @Override
    public IOncRpcSvcBuilder withGssSessionManager(GssSessionManager gssSessionManager) {
        _gssSessionManager = gssSessionManager;
        return this;
    }
    
}
