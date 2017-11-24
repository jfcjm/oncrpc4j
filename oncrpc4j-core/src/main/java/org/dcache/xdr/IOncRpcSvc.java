package org.dcache.xdr;

import java.net.InetSocketAddress;
import java.util.Set;

import org.dcache.xdr.model.itf.RpcSvcItf;
import org.glassfish.grizzly.Connection;

public interface IOncRpcSvc extends RpcSvcItf<IOncRpcSvc> {

    Set<OncRpcProgram> getPrograms();

    Connection<InetSocketAddress> getConnection();

}
