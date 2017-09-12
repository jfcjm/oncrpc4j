package org.libvirt;

import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.OncRpcSvcBuilder;

public class VirOncRpcSvcBuilder extends OncRpcSvcBuilder {

    @Override
    protected OncRpcSvc getNewOncRpcSvc(){
        return new VirOncRpcSvc(this);
    }
}
