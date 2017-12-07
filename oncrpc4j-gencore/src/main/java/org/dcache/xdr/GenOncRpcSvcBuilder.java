package org.dcache.xdr;

import org.dcache.xdr.gss.GssSessionManager;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;

public class GenOncRpcSvcBuilder extends AbstractOncRpcSvcBuilder
    <
        GenOncRpcSvc, 
        GenOncRpcCall,
        IOncRpcSvcBuilder,
        XdrTransport,
        GenOncRpcReply
    >  
    implements IOncRpcSvcBuilder
     {

    private GssSessionManager _gssManager;

    @Override
    protected IOncRpcSvcBuilder getThis() {
        return this;
    }

    @Override
    protected GenOncRpcSvc getOncRpcSvc(IOncRpcSvcBuilder builder) {
        return new GenOncRpcSvc(builder );
    }

    @Override
    public IOncRpcSvcBuilder withGssSessionManager(GssSessionManager gssSessionManager) {
        _gssManager = gssSessionManager;
        return this;
    }

    @Override
    public GssSessionManager getGssSessionManager() {
        return _gssManager;
    }

    
}
