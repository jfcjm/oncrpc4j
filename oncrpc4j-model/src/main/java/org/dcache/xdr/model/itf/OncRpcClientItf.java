package org.dcache.xdr.model.itf;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface OncRpcClientItf<
        SVC_T extends RpcSvcItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
        CALL_T extends RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
        TRANSPORT_T extends XdrTransportItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
        REPLY_T extends RpcReplyItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>> extends  AutoCloseable {

    TRANSPORT_T  connect() throws IOException;

    TRANSPORT_T connect(long timeout, TimeUnit timeUnit) throws IOException;

    void close() throws IOException;

}