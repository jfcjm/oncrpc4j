package org.libvirt;

import org.dcache.xdr.model.itf.RpcReplyItf;

public interface GenItfVirRpcReply  extends RpcReplyItf<GenVirOncRpcSvc> {

    @Override
    int getRejectStatus();

    remote_error getError();

}