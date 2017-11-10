package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.libvirt.GenItfVirtRpcCall;
import org.libvirt.GenVirOncRpcSvc;
import org.libvirt.GenVirOncRpcSvcBuilder;
import org.libvirt.GenVirRpcCall;

public class EmbeddedGenVirtServer extends  EmbeddedGenericServer<GenVirOncRpcSvc> {

    public EmbeddedGenVirtServer(int port) throws IOException {
        super(port);
    }
    @Override
    protected void processHighLevelException(RpcCallItf<GenVirOncRpcSvc> call,Exception e){
        ((GenItfVirtRpcCall)call).failRuntimeError(e);
        
    }

    @Override
    protected RpcCallItf<GenVirOncRpcSvc> createRpcCaller(int prognum, int progver,
            XdrTransportItf<GenVirOncRpcSvc> t) {
        return new GenVirRpcCall(prognum,progver,null,t);
    }

    @Override
    protected OncRpcSvcBuilderItf<GenVirOncRpcSvc> createOncSvcBuilder() {
        return new GenVirOncRpcSvcBuilder();
    }
    
}
