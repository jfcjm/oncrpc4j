package org.dcache.xdr.model.itf;

import java.io.IOException;

import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

public interface RpcProtocolFilterUtf<SVC_T extends RpcSvcItf<SVC_T>> extends Filter{

    NextAction handleRead(FilterChainContext ctx) throws IOException;

}