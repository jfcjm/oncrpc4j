/*
 * Copyright (c) 2009 - 2016 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package deprecatedclasses.oncrpc;

import java.io.EOFException;
import java.net.SocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
@Deprecated
public class ReplyQueue {

    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "timeout thread #" + counter.incrementAndGet() + " for ReplyQueue " + ReplyQueue.this);
            t.setDaemon(true);
            return t;
        }
    });
    private final ConcurrentMap<Integer, PendingRequest> _queue = new ConcurrentHashMap<>();

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
    public void registerKey(int xid, SocketAddress addr, CompletionHandler<RpcReply, XdrTransport> callback) throws EOFException {
        registerKey(xid, addr, callback, 0, null);
    }

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
    public void registerKey(int xid, SocketAddress addr, CompletionHandler<RpcReply, XdrTransport> callback, final long timeout, final TimeUnit timeoutUnits) throws EOFException {
        ScheduledFuture<?> scheduledTimeout = null;
        if (timeout > 0 && timeoutUnits != null) {
            scheduledTimeout = executorService.schedule(() -> {
                CompletionHandler<RpcReply, XdrTransport> handler = get(xid);
                if (handler != null) { //means we're 1st, no response yet
                    handler.failed(new TimeoutException("did not get a response within " + timeout + " " + timeoutUnits), null);
                }
            }, timeout, timeoutUnits);
        }
        _queue.put(xid, new PendingRequest(addr, callback, scheduledTimeout));
    }

    public void handleDisconnect(SocketAddress addr) {
        EOFException eofException = new EOFException("Disconnected");

        _queue.entrySet().stream()
                .filter(e -> e.getValue().addr.equals(addr))
                .forEach(e -> {
                    e.getValue().failed(eofException);
                    _queue.remove(e.getKey());
                });
    }

    /**
     * Get {@link CompletionHandler} for the provided xid.
     * On completion key will be unregistered.
     *
     * @param xid of rpc request.
     * @return completion handler for given xid or {@code null} if xid is unknown.
     */
    public CompletionHandler<RpcReply, XdrTransport> get(int xid) {
        PendingRequest request = _queue.remove(xid);
        if (request != null) { //means we're first. call off any pending timeouts
            request.cancelTimeout();
            return request.handler;
        } else {
            return null;
        }
    }

    private static class PendingRequest {
        private final CompletionHandler<RpcReply, XdrTransport> handler;
        private final ScheduledFuture<?> scheduledTimeout;
        private final SocketAddress addr;

        public PendingRequest(SocketAddress addr, CompletionHandler<RpcReply, XdrTransport> handler, ScheduledFuture<?> scheduledTimeout) {
            this.handler = handler;
            this.scheduledTimeout = scheduledTimeout;
            this.addr = addr;
        }

        void cancelTimeout() {
            if (scheduledTimeout != null) {
                scheduledTimeout.cancel(false);
            }
        }

        void failed(Throwable t) {
            cancelTimeout();
            handler.failed(t, null);
        }
    }

    /**
     * Shutdown all background activity, if any.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
