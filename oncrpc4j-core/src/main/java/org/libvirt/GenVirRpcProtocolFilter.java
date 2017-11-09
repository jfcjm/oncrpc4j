/*//*******************************************************************************
 * Copyright (C) 2017 INU Champollion, Albi, France
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 ******************************************************************************/

package org.libvirt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dcache.xdr.GenGrizzlyXdrTransport;
import org.dcache.xdr.GenReplyQueue;
import org.dcache.xdr.GenRpcProtocolFilter;
import org.dcache.xdr.GenRpcReply;
import org.dcache.xdr.GenXdrTransport;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAccepsStatus;
import org.dcache.xdr.RpcException;
import org.dcache.xdr.RpcMessage;
import org.dcache.xdr.RpcMessageType;
import org.dcache.xdr.Xdr;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

public class GenVirRpcProtocolFilter extends GenRpcProtocolFilter<GenVirOncRpcSvc> {

    private final static Logger _log = LoggerFactory.getLogger(GenVirRpcProtocolFilter.class);

    public GenVirRpcProtocolFilter(GenReplyQueue<GenVirOncRpcSvc> replyQueue) {
        super(replyQueue);
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        _log.debug("VirRpcProtocolFilter handle read");

        Xdr xdr = ctx.getMessage();
        _log.debug("Got an xdr message");
        if (xdr == null) {
            _log.error("Xdr message should not be null");
            return ctx.getStopAction();
        }

        xdr.beginDecoding();
        RpcMessage message = new VirRpcMessage(xdr);
        _log.debug("created a new VirRpcMessage message {},{} " + message.type(),message.xid());
        /**
         * In case of UDP grizzly does not populates connection with correct destination address.
         * We have to get peer address from the request context, which will contain SocketAddress where from
         * request was coming.
         */
        GenXdrTransport<GenVirOncRpcSvc> transport = new GenGrizzlyXdrTransport<>(ctx.getConnection(), (InetSocketAddress)ctx.getAddress(), _replyQueue);
        switch (message.type()) {
            case RpcMessageType.CALL:
            	_log.debug("Received a CALL message");
                GenVirRpcCall call = new GenVirRpcCall(message.xid(), xdr, transport);
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
                    GenVirRpcReply reply = new GenVirRpcReply(message.xid(), xdr, transport);
                    _log.debug("Rpc reply is {}",reply);
                     CompletionHandler<GenRpcReply<GenVirOncRpcSvc>, GenXdrTransport<GenVirOncRpcSvc>> callback = _replyQueue.get(message.xid());
                    if (callback != null) {
                    	_log.debug("Processing callback" + callback);
                        if (!reply.isAccepted()) {
                        	_log.warn("Reply is not accepted"+reply);
                            callback.failed(new VirRpcRejectedException(reply.getError()), transport);
                        } else if (reply.getAcceptStatus() != RpcAccepsStatus.SUCCESS) {
                        	_log.warn("Accept status failed");
                            callback.failed(new VirRpcAcceptedException(reply.getAcceptStatus()), transport);
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
                _log.warn("unknown message.type" + message.type());
                return ctx.getStopAction();
        }
    }
}
