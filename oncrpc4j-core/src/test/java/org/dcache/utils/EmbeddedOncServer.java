package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.GenOncRpcSvcBuilder;
import org.dcache.xdr.GenRpcCall;
import org.dcache.xdr.GenOncRpcSvc;
import org.dcache.xdr.RpcAuthTypeNone;
import org.dcache.xdr.model.itf.GenItfOncRpcSvcBuilder;
import org.dcache.xdr.model.itf.GenItfRpcCall;
import org.dcache.xdr.model.itf.GenItfXdrTransport;

public class EmbeddedOncServer extends  EmbeddedGenericServer<GenOncRpcSvc> {

    public EmbeddedOncServer(int i) throws IOException {
        super(i);
    }

    @Override
    protected GenItfRpcCall<GenOncRpcSvc> createRpcCaller(int prognum, int progver, GenItfXdrTransport<GenOncRpcSvc> t) {
        return new GenRpcCall(prognum, progver, new RpcAuthTypeNone(), t);
       
    }

    @Override
    protected GenItfOncRpcSvcBuilder<GenOncRpcSvc> createOncSvcBuilder() {
        return new GenOncRpcSvcBuilder();
    }
}
