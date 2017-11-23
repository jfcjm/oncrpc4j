package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.libvirt.IVirRpcCall;
import org.libvirt.VirRpcSvc;
import org.libvirt.VirRpcSvcBuilder;

public class EmbeddedGenVirtServer extends  EmbeddedGenericServer<VirRpcSvc> {

    private static final EmbbeddedGenericServerFactory<VirRpcSvc> _factory = 
            new EmbbeddedGenericServerFactory<VirRpcSvc>(){

                @Override
                public RpcCallItf<VirRpcSvc> createRpcCaller(int prognum, int progver,
                        XdrTransportItf<VirRpcSvc> transport) {
                    return IVirRpcCall.getImpl(prognum,progver,null,transport);
                }

                @Override
                public OncRpcSvcBuilderItf<VirRpcSvc> createOncSvcBuilder() {
                    return VirRpcSvcBuilder.getImpl();
                }
        
    };

    public EmbeddedGenVirtServer(int port) throws IOException {
        super(_factory,port);
    }
    @Override
    protected void processHighLevelException(RpcCallItf<VirRpcSvc> call,Exception e){
        ((IVirRpcCall)call).failRuntimeError(e);
        
    }
    
}
