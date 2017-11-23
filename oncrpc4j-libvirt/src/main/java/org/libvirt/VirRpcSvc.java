package org.libvirt;

import java.io.IOException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.model.itf.RpcSvcItf;

public interface VirRpcSvc  extends RpcSvcItf<VirRpcSvc>{

    static VirRpcSvc getImpl(VirRpcSvcBuilder builder) {
        return new VirRpcSvcImpl(builder);
    }

    @Override
    void start() throws IOException;

    void setPacketWrapper(SASLPacketWrapper sc) throws OncRpcException;

    void setPacketWrapperAfterNextWrite(SASLPacketWrapper pw);

    void unsetPacketWrapper();

}