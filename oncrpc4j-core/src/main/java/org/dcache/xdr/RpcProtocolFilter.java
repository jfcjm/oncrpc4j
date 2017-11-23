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
import org.dcache.xdr.OncRpcAcceptedException;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcRejectedException;
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.RpcAccepsStatus;
import org.dcache.xdr.RpcException;
import org.dcache.xdr.RpcMessageType;
import org.dcache.xdr.RpcProtocolFilter;
import org.dcache.xdr.RpcReply;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcProtocolFilter;
import org.dcache.xdr.model.root.RpcMessage;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

public final class RpcProtocolFilter extends AbstractRpcProtocolFilter<OncRpcSvc> implements  IRpcProtocolFilter{

    private final static Logger _log = LoggerFactory.getLogger(RpcProtocolFilter.class);

    public RpcProtocolFilter(ReplyQueueItf<OncRpcSvc> replyQueue) {
        super(replyQueue);
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfRpcProtocolFilter#handleRead(org.glassfish.grizzly.filterchain.FilterChainContext)
     */
    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {

        Xdr xdr = ctx.getMessage();
        if (xdr == null) {
            _log.error("Parser returns bad XDR");
            return ctx.getStopAction();
        }

        xdr.beginDecoding();

        //TODO POst ABSTRACTION 
        RpcMessage message = new RpcMessage(xdr);
        /**
         * In case of UDP grizzly does not populates connection with correct destination address.
         * We have to get peer address from the request context, which will contain SocketAddress where from
         * request was coming.
         */
        //XdrTransportItf<OncRpcSvc> transport = new OncGrizzlyXdrTransportImpl<OncRpcSvc>(ctx.getConnection(), (InetSocketAddress)ctx.getAddress(), _replyQueue);
        XdrTransportItf<OncRpcSvc> transport = XdrTransport.getImpl(ctx.getConnection(), (InetSocketAddress)ctx.getAddress(), _replyQueue);
        switch (message.type()) {
            case RpcMessageType.CALL:
                _log.debug("Received a CALL message");
                RpcCallItf<OncRpcSvc> call = new RpcCall(message.xid(), xdr, transport);
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
                _log.debug("Received a Reply message with xid {} ",message.xid());
                try {
                    RpcReply reply =  RpcReply.getImpl(message.xid(), xdr, transport);
                    _log.debug("Got a reply, status {}",reply.getAcceptStatus());
                    _log.debug("Rpc reply is {}",reply);
                     CompletionHandler<RpcReplyItf<OncRpcSvc>, XdrTransportItf<OncRpcSvc>> callback = _replyQueue.get(message.xid());
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


    @Override
    protected RpcReplyItf<OncRpcSvc> createReply(Xdr xdr, RpcMessage message,
            XdrTransportItf<OncRpcSvc> transport) throws OncRpcException, IOException {
        return  RpcReply.getImpl(message.xid(), xdr, transport);
    }
}
