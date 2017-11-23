package org.dcache.xdr;

import org.dcache.xdr.model.itf.ImplementationGetterItf;
import org.dcache.xdr.model.itf.RpcMessageParserTCPItf;

public interface IRpcMessageParserTCP extends RpcMessageParserTCPItf<OncRpcSvc> ,  ImplementationGetterItf{

    static RpcMessageParserTCP getImpl() {
        return new RpcMessageParserTCP();
    }

}
