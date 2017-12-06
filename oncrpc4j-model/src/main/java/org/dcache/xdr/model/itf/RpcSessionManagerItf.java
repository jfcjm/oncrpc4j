package org.dcache.xdr.model.itf;

import org.glassfish.grizzly.filterchain.Filter;

public interface RpcSessionManagerItf<SVC_T extends RpcSvcItf<SVC_T,CALL_T>,CALL_T extends RpcCallItf<SVC_T,CALL_T>> extends Filter {
    
}
