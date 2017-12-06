package org.dcache.generics.alt.dispatchable;

import java.io.IOException;

import org.dcache.xdr.MismatchInfo;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.model.itf.RpcCallItf;

public interface RpcReplyAltItf<SVC_T extends RpcSvcAltItf<SVC_T,CALL_T>, CALL_T extends RpcCallAltItf<SVC_T,CALL_T>> {

    boolean isAccepted();

    int getAcceptStatus();

    MismatchInfo getMismatchInfo();

    int getAuthStatus();

    int getRejectStatus();

    void getReplyResult(XdrAble result) throws OncRpcException, IOException;

    String toString();

}