package org.dcache.xdr;

import org.dcache.xdr.gss.GssSessionManager;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;

public class GenOncRpcSvcBuilder<SVC_T extends RpcSvcItf<IOncRpcSvc>> extends AbstractOncRpcSvcBuilder<IOncRpcSvc, IOncRpcSvcBuilder> implements IOncRpcSvcBuilder{
    GssSessionManager _gssSessionManager =null;
    private int _protocol;
    @Override
    public IOncRpcSvcBuilder withGssSessionManager(GssSessionManager gssSessionManager) {
        _gssSessionManager = gssSessionManager;
        return this;
    }

    @Override
    public GssSessionManager getGssSessionManager() {
        return _gssSessionManager;
    }

    @Override
    protected IOncRpcSvcBuilder getThis() {
        return this;
    }

    @Override
    protected IOncRpcSvc getOncRpcSvc(IOncRpcSvcBuilder builder_T) {
        return new GenOncRpcSvc<>(this);
    }

    @Override
    public IOncRpcSvcBuilder withIpProtocolType(int protocol) {
        super.withIpProtocolType(protocol);
        return this;
    }
}
