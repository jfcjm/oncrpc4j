package org.dcache.generics.alt.dispatchable;

import org.glassfish.grizzly.filterchain.BaseFilter;

public class DefaultSessionManagerAlt<SVC_T extends RpcSvcAltItf<SVC_T,CALL_T>,CALL_T extends RpcCallAltItf<SVC_T,CALL_T>> extends BaseFilter implements RpcSessionManagerAltItf<SVC_T,CALL_T> {

}
