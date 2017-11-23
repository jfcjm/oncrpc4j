package org.dcache.xdr;

import org.dcache.xdr.impl.RpcMessageParserUDPImpl;
import org.dcache.xdr.model.itf.ImplementationGetterItf;
import org.dcache.xdr.model.itf.RpcMessageParserTCPItf;
import org.glassfish.grizzly.filterchain.Filter;

public interface RpcMessageParserUDP extends RpcMessageParserTCPItf<OncRpcSvc> ,  ImplementationGetterItf{

    static Filter getImpl() {
        return new RpcMessageParserUDPImpl();
    }

}
