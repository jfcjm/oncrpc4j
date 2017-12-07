package org.dcache.utils;

import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public interface EmbbeddedGenericServerFactory<
            SVC_T extends RpcSvcItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
            CALL_T extends RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
            BUILDER_T extends OncRpcSvcBuilderItf<SVC_T,CALL_T,BUILDER_T,TRANSPORT_T,REPLY_T>,
            TRANSPORT_T extends XdrTransportItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
            REPLY_T extends RpcReplyItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>
            > {

    public RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> createRpcCaller(int prognum, int progver, XdrTransportItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> transport) ;

    public  BUILDER_T createOncSvcBuilder();

}
