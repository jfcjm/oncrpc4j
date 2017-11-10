package org.dcache.xdr.model.itf;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface GenItfOncRpcClient<SVC_T extends GenItfRpcSvc<SVC_T>> extends  AutoCloseable {

    GenItfXdrTransport<SVC_T> connect() throws IOException;

    GenItfXdrTransport<SVC_T> connect(long timeout, TimeUnit timeUnit) throws IOException;

    void close() throws IOException;

}