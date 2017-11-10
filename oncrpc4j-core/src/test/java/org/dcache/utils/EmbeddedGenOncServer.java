package org.dcache.utils;

import java.io.IOException;
import org.dcache.xdr.OncRpcSvcBuilder;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.RpcAuthTypeNone;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public class EmbeddedGenOncServer extends  EmbeddedGenericServer<OncRpcSvc> {

    public EmbeddedGenOncServer(int i) throws IOException {
        super(i);
    }

    @Override
    protected RpcCallItf<OncRpcSvc> createRpcCaller(int prognum, int progver, XdrTransportItf<OncRpcSvc> t) {
        return new RpcCall(prognum, progver, new RpcAuthTypeNone(), t);
       
    }

    @Override
    protected OncRpcSvcBuilderItf<OncRpcSvc> createOncSvcBuilder() {
         OncRpcSvcBuilder result = new OncRpcSvcBuilder().withTCP().withoutAutoPublish();
         return result;
    }
}
