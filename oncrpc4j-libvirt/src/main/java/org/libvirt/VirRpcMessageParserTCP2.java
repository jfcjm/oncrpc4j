package org.libvirt;

import org.dcache.xdr.model.itf.RpcMessageParserTCPItf;

public interface VirRpcMessageParserTCP2 extends RpcMessageParserTCPItf<VirRpcSvc> {

    static VirRpcMessageParserTCP2 getImpl() {
        return new VirRpcMessageParserTCP2Impl();
    }

}
