package org.dcache.generics.alt.dispatchable;

import org.glassfish.grizzly.filterchain.Filter;

public interface RpcSessionManagerAltItf<SVC_T extends RpcSvcAltItf<SVC_T,CALL_T>,CALL_T extends RpcCallAltItf<SVC_T,CALL_T>> extends Filter {
    
}
