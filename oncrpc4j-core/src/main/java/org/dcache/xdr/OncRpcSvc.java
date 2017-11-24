package org.dcache.xdr;

import java.net.InetSocketAddress;
import java.util.Set;

import org.dcache.xdr.model.root.AbstractOncRpcSvc;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;
import org.glassfish.grizzly.Connection;

public class OncRpcSvc extends AbstractOncRpcSvc<IOncRpcSvc> implements IOncRpcSvc{
    
    OncRpcSvc(AbstractOncRpcSvcBuilder<IOncRpcSvc> builder) {
        super(builder);
    }

    @Override
    public Connection<InetSocketAddress> getConnection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<OncRpcProgram> getPrograms() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
