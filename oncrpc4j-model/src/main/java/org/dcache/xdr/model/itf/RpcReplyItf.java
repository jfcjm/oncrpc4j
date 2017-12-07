package org.dcache.xdr.model.itf;

import java.io.IOException;

import org.dcache.xdr.MismatchInfo;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrAble;

public interface RpcReplyItf<
    SVC_T extends RpcSvcItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>, 
    CALL_T extends RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
    TRANSPORT_T extends XdrTransportItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
    REPLY_T extends RpcReplyItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>
    > {

    boolean isAccepted();

    int getAcceptStatus();

    MismatchInfo getMismatchInfo();

    int getAuthStatus();

    int getRejectStatus();

    void getReplyResult(XdrAble result) throws OncRpcException, IOException;

    String toString();

    REPLY_T getThis();

}