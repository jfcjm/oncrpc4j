package org.libvirt;

import java.io.IOException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public interface VirRpcReply  extends RpcReplyItf<VirRpcSvc> {

    @Override
    int getRejectStatus();

    remote_error getError();
    public static VirRpcReply  getImpl(int xid, Xdr xdr, XdrTransportItf<VirRpcSvc> transport) throws OncRpcException, IOException{
        return new VirRpcReplyImpl(xid, xdr, transport);
    }                                                                                  
}