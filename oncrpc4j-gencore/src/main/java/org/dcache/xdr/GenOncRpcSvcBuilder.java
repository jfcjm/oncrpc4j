package org.dcache.xdr;

import org.dcache.xdr.gss.GssSessionManager;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcDispatchableItf;
import org.dcache.xdr.model.itf.RpcSessionManagerItf;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;

public class GenOncRpcSvcBuilder extends AbstractOncRpcSvcBuilder
    <
        GenOncRpcSvc, 
        GenOncRpcCall,
        IOncRpcSvcBuilder,
        XdrTransport,
        GenRpcReply
    >  
    implements IOncRpcSvcBuilder  {

    @Override
    public IOncRpcSvcBuilder withRpcService(OncRpcProgram program,
            RpcDispatchableItf<IOncRpcSvc, IOncRpcCall, IOncTransport, IOncReply> service) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IOncRpcSvcBuilder withRpcSessionManager(
            RpcSessionManagerItf<IOncRpcSvc, IOncRpcCall, IOncTransport, IOncReply> sessionManager) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GenOncRpcSvc build() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IOncRpcSvcBuilder withGssSessionManager(GssSessionManager gssSessionManager) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GssSessionManager getGssSessionManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected GenOncRpcSvcBuilder getThis() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected GenOncRpcSvc getOncRpcSvc(GenOncRpcSvcBuilder builder_T) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
