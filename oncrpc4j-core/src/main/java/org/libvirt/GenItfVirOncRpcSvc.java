package org.libvirt;

import java.io.IOException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.model.itf.RpcSvcItf;

public interface GenItfVirOncRpcSvc  extends RpcSvcItf<GenVirOncRpcSvc>{

    @Override
    void start() throws IOException;

    void setPacketWrapper(SASLPacketWrapper sc) throws OncRpcException;

    void setPacketWrapperAfterNextWrite(SASLPacketWrapper pw);

}