package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.GenOncRpcSvcBuilder;
import org.dcache.xdr.GenRpcCall;
import org.dcache.xdr.GenXdrTransport;
import org.libvirt.GenVirOncRpcSvc;
import org.libvirt.GenVirOncRpcSvcBuilder;
import org.libvirt.GenVirRpcCall;

public class EmbeddedGenVirtServer extends  EmbeddedGenericServer<GenVirOncRpcSvc> {

    public EmbeddedGenVirtServer(int port) throws IOException {
        super(port);
    }
    @Override
    protected void processHighLevelException(GenRpcCall<GenVirOncRpcSvc> call,Exception e){
        ((GenVirRpcCall)call).failRuntimeError(e);
        
    }

    @Override
    protected GenRpcCall<GenVirOncRpcSvc> createRpcCaller(int prognum, int progver,
            GenXdrTransport<GenVirOncRpcSvc> t) {
        return new GenVirRpcCall(prognum,progver,null,t);
    }

    @Override
    protected GenOncRpcSvcBuilder<GenVirOncRpcSvc> createOncSvcBuilder() {
        return new GenVirOncRpcSvcBuilder() {
            
            @Override
            protected GenVirOncRpcSvc getNewOncRpcSvc() {
                return new GenVirOncRpcSvc(this);
            }
        };
    }
    
}
