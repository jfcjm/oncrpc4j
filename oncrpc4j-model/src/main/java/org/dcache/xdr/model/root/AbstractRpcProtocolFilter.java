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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dcache.xdr.OncRpcAcceptedException;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcRejectedException;
import org.dcache.xdr.RpcAccepsStatus;
import org.dcache.xdr.RpcException;
import org.dcache.xdr.RpcMessageType;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

public abstract class  AbstractRpcProtocolFilter
    <
        SVC_T extends RpcSvcItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
        CALL_T extends RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
        TRANSPORT_T extends XdrTransportItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
        REPLY_T extends RpcReplyItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>
        > extends BaseFilter {

    private final static Logger _log = LoggerFactory.getLogger(AbstractRpcProtocolFilter.class);
    private final ReplyQueueItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> _replyQueue;
    //TODO private ProtocolFactoryItf<SVC_T> _protoFactory;

    public AbstractRpcProtocolFilter(ReplyQueueItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> replyQueue /*, ProtocolFactoryItf<SVC_T> protoFactory*/) {
        _replyQueue = replyQueue;
        //TODO _protoFactory = protoFactory;
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {

        Xdr xdr = ctx.getMessage();
        if (xdr == null) {
            _log.error("Parser returns bad XDR");
            return ctx.getStopAction();
        }

        xdr.beginDecoding();
        final HeaderItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> header = new AbstractRpcMessage<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>(xdr);
        /**
         * In case of UDP grizzly does not populates connection with correct destination address.
         * We have to get peer address from the request context, which will contain SocketAddress where from
         * request was coming.
         */
        //TODO  XdrTransportItf<SVC_T> transport = new AbstractGrizzlyXdrTransport<>(ctx.getConnection(), (InetSocketAddress)ctx.getAddress(), _replyQueue, _protoFactory);
       TRANSPORT_T transport = createTransport(ctx.getConnection(), (InetSocketAddress)ctx.getAddress(), _replyQueue).getThis();
        switch (header.getMessageType()) {
            case RpcMessageType.CALL:
                RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> call = createRpcCall(header, xdr, transport);
                try {
                    call.accept();
                    ctx.setMessage(call);

                } catch (RpcException e) {
                    call.reject(e.getStatus(), e.getRpcReply());
                    _log.info("RPC request rejected: {}", e.getMessage());
                    return ctx.getStopAction();
                } catch (OncRpcException e) {
                    _log.info("failed to process RPC request: {}", e.getMessage());
                    return ctx.getStopAction();
                }
                return ctx.getInvokeAction();
            case RpcMessageType.REPLY:
                try {
                      REPLY_T reply = createRpcReply(xdr, header, transport).getThis();
                      CompletionHandler<REPLY_T, TRANSPORT_T> callback = _replyQueue.get(header.getXid());
                    if (callback != null) {
                        if (!reply.isAccepted()) {
                            callback.failed(new OncRpcRejectedException(reply.getRejectStatus()), transport);
                        } else if (reply.getAcceptStatus() != RpcAccepsStatus.SUCCESS) {
                            callback.failed(new OncRpcAcceptedException(reply.getAcceptStatus()), transport);
                        } else {
                            callback.completed(reply, transport);
                        }
                    }
                } catch (OncRpcException e) {
                    _log.warn("failed to decode reply:", e);
                }
                return ctx.getStopAction();
            default:
                // bad XDR
                return ctx.getStopAction();
        }
    }

    protected abstract  AbstractRpcReply<SVC_T, CALL_T, TRANSPORT_T,REPLY_T> createRpcReply(Xdr xdr, final HeaderItf<SVC_T, CALL_T, TRANSPORT_T, REPLY_T> header,
            XdrTransportItf<SVC_T, CALL_T, TRANSPORT_T, REPLY_T> transport) throws OncRpcException, IOException;
    //return new AbstractRpcReply<>(header, xdr, transport);

    


    
    protected abstract XdrTransportItf<SVC_T, CALL_T, TRANSPORT_T,REPLY_T> createTransport(Connection connection,
            InetSocketAddress address, ReplyQueueItf<SVC_T, CALL_T, TRANSPORT_T,REPLY_T> _replyQueue2);
    /*
    {
        return new AbstractGrizzlyXdrTransport<>(ctx.getConnection(), (InetSocketAddress)ctx.getAddress(), _replyQueue);
    }
    */
    protected abstract RpcCallItf<SVC_T, CALL_T,TRANSPORT_T,REPLY_T> createRpcCall(HeaderItf<SVC_T, CALL_T,TRANSPORT_T,REPLY_T> header, Xdr xdr,
            XdrTransportItf<SVC_T, CALL_T,TRANSPORT_T,REPLY_T> transport);
}
