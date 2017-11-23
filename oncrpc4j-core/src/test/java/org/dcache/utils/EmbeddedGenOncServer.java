package org.dcache.utils;

import java.io.IOException;
import org.dcache.xdr.IOncRpcSvcBuilder;
import org.dcache.xdr.IRpcCall;
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.RpcAuthTypeNone;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public class EmbeddedGenOncServer extends  EmbeddedGenericServer<OncRpcSvc> {
    
    private static final EmbbeddedGenericServerFactory<OncRpcSvc> factory = 
            new EmbbeddedGenericServerFactory<OncRpcSvc>(){

                @Override
                public RpcCallItf<OncRpcSvc> createRpcCaller(int prognum, int progver,
                        XdrTransportItf<OncRpcSvc> transport) {
                    return IRpcCall.getImpl(prognum, progver, new RpcAuthTypeNone(), transport);
                }

                @Override
                public OncRpcSvcBuilderItf<OncRpcSvc> createOncSvcBuilder() {
                    return  IOncRpcSvcBuilder.getImpl().withTCP().withoutAutoPublish();
                }
        
    };

    public EmbeddedGenOncServer(int i) throws IOException {
        super(factory,i);
    }
}
