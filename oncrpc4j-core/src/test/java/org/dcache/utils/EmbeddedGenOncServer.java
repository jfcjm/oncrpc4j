package org.dcache.utils;

import java.io.IOException;
import org.dcache.xdr.GenOncRpcSvcBuilder;
import org.dcache.xdr.GenRpcCall;
import org.dcache.xdr.GenOncRpcSvc;
import org.dcache.xdr.RpcAuthTypeNone;
import org.dcache.xdr.model.itf.GenItfOncRpcSvcBuilder;
import org.dcache.xdr.model.itf.GenItfRpcCall;
import org.dcache.xdr.model.itf.GenItfXdrTransport;

public class EmbeddedGenOncServer extends  EmbeddedGenericServer<GenOncRpcSvc> {

    public EmbeddedGenOncServer(int i) throws IOException {
        super(i);
    }

    @Override
    protected GenItfRpcCall<GenOncRpcSvc> createRpcCaller(int prognum, int progver, GenItfXdrTransport<GenOncRpcSvc> t) {
        return new GenRpcCall(prognum, progver, new RpcAuthTypeNone(), t);
       
    }

    @Override
    protected GenItfOncRpcSvcBuilder<GenOncRpcSvc> createOncSvcBuilder() {
         GenOncRpcSvcBuilder result = new GenOncRpcSvcBuilder().withTCP().withoutAutoPublish();
         return result;
    }
}
