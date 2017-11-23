package org.dcache.xdr.model.itf;

import java.io.IOException;

import org.dcache.xdr.MismatchInfo;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrAble;

public interface RpcReplyItf<SVC_T extends RpcSvcItf<SVC_T>> {

    boolean isAccepted();

    int getAcceptStatus();

    MismatchInfo getMismatchInfo();

    int getAuthStatus();

    int getRejectStatus();

    void getReplyResult(XdrAble result) throws OncRpcException, IOException;

    String toString();

    <ERR_T> ERR_T getError();

}