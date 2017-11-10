package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.model.itf.GenItfOncRpcSvcBuilder;
import org.dcache.xdr.model.itf.GenItfRpcCall;
import org.dcache.xdr.model.itf.GenItfXdrTransport;
import org.dcache.xdr.model.itf.GenXdrTransport;
import org.libvirt.GenItfVirtRpcCall;
import org.libvirt.GenVirOncRpcSvc;
import org.libvirt.GenVirOncRpcSvcBuilder;
import org.libvirt.GenVirRpcCall;

public class EmbeddedGenVirtServer extends  EmbeddedGenericServer<GenVirOncRpcSvc> {

    public EmbeddedGenVirtServer(int port) throws IOException {
        super(port);
    }
    @Override
    protected void processHighLevelException(GenItfRpcCall<GenVirOncRpcSvc> call,Exception e){
        ((GenItfVirtRpcCall)call).failRuntimeError(e);
        
    }

    @Override
    protected GenItfRpcCall<GenVirOncRpcSvc> createRpcCaller(int prognum, int progver,
            GenItfXdrTransport<GenVirOncRpcSvc> t) {
        return new GenVirRpcCall(prognum,progver,null,t);
    }

    @Override
    protected GenItfOncRpcSvcBuilder<GenVirOncRpcSvc> createOncSvcBuilder() {
        return new GenVirOncRpcSvcBuilder();
    }
    
}
