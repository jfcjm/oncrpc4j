package org.libvirt;

import org.dcache.xdr.model.itf.GenItfRpcReply;

public interface GenItfVirRpcReply  extends GenItfRpcReply<GenVirOncRpcSvc> {

    @Override
    int getRejectStatus();

    remote_error getError();

}