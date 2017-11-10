package org.libvirt;

import java.io.IOException;

import org.dcache.xdr.model.itf.RpcProtocolFilterUtf;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

public interface GenItfVirRpcProtcolFilter extends RpcProtocolFilterUtf<GenVirOncRpcSvc>{

    NextAction handleRead(FilterChainContext ctx) throws IOException;

}