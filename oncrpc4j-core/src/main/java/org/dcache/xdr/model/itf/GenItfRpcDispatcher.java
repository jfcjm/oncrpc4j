package org.dcache.xdr.model.itf;

import java.io.IOException;

import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

public interface GenItfRpcDispatcher<SVC_T extends GenItfRpcSvc<SVC_T>> {

    NextAction handleRead(FilterChainContext ctx) throws IOException;

}