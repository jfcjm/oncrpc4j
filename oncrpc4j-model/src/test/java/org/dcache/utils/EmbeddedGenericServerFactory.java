package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public interface EmbeddedGenericServerFactory
        <
        SVC_T extends RpcSvcItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
        CALL_T extends RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
        BUILDER_T extends OncRpcSvcBuilderItf<SVC_T,CALL_T,BUILDER_T,TRANSPORT_T,REPLY_T>,
        TRANSPORT_T extends XdrTransportItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
        REPLY_T extends RpcReplyItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>
        
        > {

    public EmbeddedGenericServer<SVC_T,CALL_T,BUILDER_T,TRANSPORT_T,REPLY_T> createEmbeddedServer(int port) throws IOException ;

}
