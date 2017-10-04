package org.libvirt;

import org.dcache.xdr.OncRpcSvcBuilder;

public class VirOncRpcSvcBuilder extends OncRpcSvcBuilder {

    @Override
    protected VirOncRpcSvc getNewOncRpcSvc(){
        return new VirOncRpcSvc(this);
    }
    

    @Override
    public OncRpcSvcBuilder withAutoPublish() {
        throw new RuntimeException("Libvirt does not publish its service through a portmapper");
    }
    @Override
    public OncRpcSvcBuilder withoutAutoPublish() {
        throw new RuntimeException("Libvirt does not publish its service through a portmapper");
    }
    
    
}
