package org.dcache.xdr;


import org.dcache.xdr.model.itf.XdrTransportItf;

public interface XdrTransport extends XdrTransportItf<OncRpcSvc, RpcCall> {
         
    XdrTransport  getThis();

}
