package org.dcache.xdr.model.itf;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface OncRpcClientItf<SVC_T extends RpcSvcItf<SVC_T>> extends  AutoCloseable {

    XdrTransportItf<SVC_T> connect() throws IOException;

    XdrTransportItf<SVC_T> connect(long timeout, TimeUnit timeUnit) throws IOException;

    void close() throws IOException;

}