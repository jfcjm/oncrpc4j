package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.GenOncRpcSvcBuilder;
import org.dcache.xdr.GenRpcCall;
import org.dcache.xdr.GenXdrTransport;
import org.dcache.xdr.RpcAuthTypeNone;

public class EmbeddedOncServer extends  EmbeddedGenericServer<FromGenOncRpcSvc> {

    public EmbeddedOncServer() throws IOException {
        super();
    }

    public EmbeddedOncServer(int i) throws IOException {
        super(i);
    }

    @Override
    protected GenOncRpcSvcBuilder<FromGenOncRpcSvc> createOncSvcBuilder() {
        return new GenOncRpcSvcBuilder<FromGenOncRpcSvc>(){

            @Override
            protected FromGenOncRpcSvc getNewOncRpcSvc() {
                return new FromGenOncRpcSvc(this);
            }
            
        }.withoutAutoPublish();
    }

    @Override
    protected GenRpcCall<FromGenOncRpcSvc> createRpcCaller(int prognum, int progver, GenXdrTransport<FromGenOncRpcSvc> t) {
       return new GenRpcCall<FromGenOncRpcSvc>(prognum, progver, new RpcAuthTypeNone(), t);
    }

}
