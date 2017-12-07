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
package org.dcache.xdr.model.root;

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
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public  class AbstractReplyQueue
    <
        SVC_T extends RpcSvcItf<SVC_T,
        CALL_T,TRANSPORT_T,REPLY_T>,
        CALL_T extends RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
        TRANSPORT_T extends XdrTransportItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
        REPLY_T extends RpcReplyItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> > implements ReplyQueueItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> {

    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "timeout thread #" + counter.incrementAndGet() + " for ReplyQueue " + AbstractReplyQueue.this);
            t.setDaemon(true);
            return t;
        }
    });
    private final ConcurrentMap<Integer, PendingRequest> _queue = new ConcurrentHashMap<>();

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfReplyQueue#registerKey(int, java.net.SocketAddress, java.nio.channels.CompletionHandler)
     */
    @Override
    public void registerKey(int xid, SocketAddress addr, CompletionHandler<REPLY_T,TRANSPORT_T>  callback) throws EOFException {
        registerKey(xid, addr, callback, 0, null);
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfReplyQueue#registerKey(int, java.net.SocketAddress, java.nio.channels.CompletionHandler, long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void registerKey(int xid, SocketAddress addr, CompletionHandler<REPLY_T,TRANSPORT_T> callback, final long timeout, final TimeUnit timeoutUnits) throws EOFException {
        ScheduledFuture<?> scheduledTimeout = null;
        if (timeout > 0 && timeoutUnits != null) {
            scheduledTimeout = executorService.schedule(() -> {
                CompletionHandler<? extends RpcReplyItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,? extends XdrTransportItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>> handler = get(xid);
                if (handler != null) { //means we're 1st, no response yet
                    handler.failed(new TimeoutException("did not get a response within " + timeout + " " + timeoutUnits), null);
                }
            }, timeout, timeoutUnits);
        }
        _queue.put(xid, new PendingRequest(addr, callback, scheduledTimeout));
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfReplyQueue#handleDisconnect(java.net.SocketAddress)
     */
    @Override
    public void handleDisconnect(SocketAddress addr) {
        EOFException eofException = new EOFException("Disconnected");

        _queue.entrySet().stream()
                .filter(e -> e.getValue().addr.equals(addr))
                .forEach(e -> {
                    e.getValue().failed(eofException);
                    _queue.remove(e.getKey());
                });
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfReplyQueue#get(int)
     */
    @Override
    public CompletionHandler<REPLY_T,TRANSPORT_T> get(int xid) {
        PendingRequest request = _queue.remove(xid);
        if (request != null) { //means we're first. call off any pending timeouts
            request.cancelTimeout();
            return request.handler;
        } else {
            return null;
        }
    }

    private  class PendingRequest {
        private final CompletionHandler<REPLY_T,TRANSPORT_T> handler;
        private final ScheduledFuture<?> scheduledTimeout;
        private final SocketAddress addr;

        public PendingRequest(SocketAddress addr, CompletionHandler<REPLY_T,TRANSPORT_T> handler, ScheduledFuture<?> scheduledTimeout) {
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

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfReplyQueue#shutdown()
     */
    @Override
    public void shutdown() {
        executorService.shutdown();
    }
}
