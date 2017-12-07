package org.dcache.generics.alt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public interface RpcSvcAltItf<SVC_T extends RpcSvcItf<SVC_T>,CALL_T extends RpcCallItf<SVC_T>>  {

    /**
     * Register a new PRC service. Existing registration will be overwritten.
     *
     * @param prog program number
     * @param handler RPC requests handler.
     */
    void register(OncRpcProgram prog, RpcDispatchableAltItf<SVC_T,CALL_T> handler);

    /**
     * Unregister program.
     *
     * @param prog
     */
    void unregister(OncRpcProgram prog);

    /**
     * Add programs to existing services.
     * @param services
     * @deprecated use {@link OncRpcSvcBuilder#withRpcService} instead.
     */
    void setPrograms(Map<OncRpcProgram, RpcDispatchableAltItf<SVC_T,CALL_T>> services);

    void start() throws IOException;

    void stop() throws IOException;

    void stop(long gracePeriod, TimeUnit timeUnit) throws IOException;

    XdrTransportItf<SVC_T> connect(InetSocketAddress socketAddress) throws IOException;

    XdrTransportItf<SVC_T> connect(InetSocketAddress socketAddress, long timeout, TimeUnit timeUnit) throws IOException;

    /**
     * Returns the address of the endpoint this service is bound to,
     * or <code>null</code> if it is not bound yet.
     * @param protocol
     * @return a {@link InetSocketAddress} representing the local endpoint of
     * this service, or <code>null</code> if it is not bound yet.
     */
    InetSocketAddress getInetSocketAddress(int protocol);

}