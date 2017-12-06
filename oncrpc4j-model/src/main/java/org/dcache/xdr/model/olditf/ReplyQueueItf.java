package org.dcache.xdr.model.itf;

import java.io.EOFException;
import java.net.SocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

public interface ReplyQueueItf<SVC_T extends RpcSvcItf<SVC_T>> {

    /**
     * Register callback handler for a given xid. The Callback is called when
     * client receives reply from the server, request failed of expired.
     *
     * @param xid xid of RPC request.
     * @param addr socket address of remote endpoint.
     * @param callback completion handler which will be used when request execution is
     * finished.
     * @throws EOFException if disconnected
     */
    void registerKey(int xid, SocketAddress addr,
            CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback) throws EOFException;

    /**
     * Register callback handler for a given xid. The Callback is called when
     * client receives reply from the server, request failed of expired.
     *
     * @param xid xid of RPC request.
     * @param addr socket address of remote endpoint.
     * @param callback completion handler which will be used when request execution is
     * finished.
     * @param timeout how long client is interested in the reply.
     * @param timeoutUnits units in which timeout value is expressed.
     * @throws EOFException if disconnected
     */
    void registerKey(int xid, SocketAddress addr,
            CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback, long timeout, TimeUnit timeoutUnits)
            throws EOFException;


    void handleDisconnect(SocketAddress addr);

    /**
     * Get {@link CompletionHandler} for the provided xid.
     * On completion key will be unregistered.
     *
     * @param xid of rpc request.
     * @return completion handler for given xid or {@code null} if xid is unknown.
     */
    CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> get(int xid);

    /**
     * Shutdown all background activity, if any.
     */
    void shutdown();

}