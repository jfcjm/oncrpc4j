package org.libvirt;

import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;

public interface VirRpcSvcBuilder extends OncRpcSvcBuilderItf<VirRpcSvc>{

    static  VirRpcSvcBuilder getImpl() {
        return new VirRpcSvcBuilderImpl();
    }

    OncRpcSvcBuilderItf<VirRpcSvc> withTCP();

}
