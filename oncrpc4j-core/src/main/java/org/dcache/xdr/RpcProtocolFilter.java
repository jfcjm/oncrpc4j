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
package org.dcache.xdr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

public class RpcProtocolFilter extends BaseFilter {

    private final static Logger _log = LoggerFactory.getLogger(RpcProtocolFilter.class);
    protected final ReplyQueue _replyQueue;

    public RpcProtocolFilter(ReplyQueue replyQueue) {
        _replyQueue = replyQueue;
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {

        Xdr xdr = ctx.getMessage();
        if (xdr == null) {
            _log.error("Parser returns bad XDR");
            return ctx.getStopAction();
        }

        xdr.beginDecoding();

        RpcMessage message = new RpcMessage(xdr);
        /**
         * In case of UDP grizzly does not populates connection with correct destination address.
         * We have to get peer address from the request context, which will contain SocketAddress where from
         * request was coming.
         */
        XdrTransport transport = new GrizzlyXdrTransport(ctx.getConnection(), (InetSocketAddress)ctx.getAddress(), _replyQueue);

        switch (message.type()) {
            case RpcMessageType.CALL:
            	_log.debug("Received a CALL message");
                RpcCall call = new RpcCall(message.xid(), xdr, transport);
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
            	_log.debug("Received a Reply message with xid {}",message.xid());
                try {
                    RpcReply reply = new RpcReply(message.xid(), xdr, transport);
                    _log.debug("Rpc reply is {}",reply);
                    CompletionHandler<RpcReply, XdrTransport> callback = _replyQueue.get(message.xid());
                    if (callback != null) {
                    	_log.debug("Processing callback");
                        if (!reply.isAccepted()) {
                        	_log.debug("Reply is not accepted");
                            callback.failed(new OncRpcRejectedException(reply.getRejectStatus()), transport);
                        } else if (reply.getAcceptStatus() != RpcAccepsStatus.SUCCESS) {
                        	_log.debug("Accept status failed");
                            callback.failed(new OncRpcAcceptedException(reply.getAcceptStatus()), transport);
                        } else {
                        	_log.debug("Callback completed");
                            callback.completed(reply, transport);
                        }
                    } else {
                    	_log.debug("No callback to process");
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
}
