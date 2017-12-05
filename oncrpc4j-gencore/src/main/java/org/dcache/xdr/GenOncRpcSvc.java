package org.dcache.xdr;

import java.net.InetSocketAddress;
import java.util.Set;

import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.root.AbstractOncRpcSvc;
import org.glassfish.grizzly.Connection;
/**
 * Problème de type
 * @author jmk
 *
 * @param <SVC_T>
 */
public class GenOncRpcSvc<SVC_T extends RpcSvcItf<IOncRpcSvc>> extends AbstractOncRpcSvc<IOncRpcSvc> implements IOncRpcSvc{

    protected <BUILDER_T extends IOncRpcSvcBuilder> GenOncRpcSvc(BUILDER_T builder) {
        super(builder);
    }

    @Override
    public IOncRpcSvc getThis() {
        return this;
    }

}
