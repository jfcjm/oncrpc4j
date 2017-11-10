/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

import org.dcache.xdr.model.itf.GenItfReplyQueue;
import org.dcache.xdr.model.itf.GenItfRpcReply;
import org.dcache.xdr.model.itf.GenItfXdrTransport;
import org.dcache.xdr.model.itf.GenXdrTransport;
import org.dcache.xdr.model.root.GenAbstractRpcCall;
import org.dcache.xdr.model.root.RpcMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GenRpcCall extends GenAbstractRpcCall<GenOncRpcSvc>  {
    final static Logger _log = LoggerFactory.getLogger(GenRpcCall.class);
    

    public GenRpcCall(int prog, int ver, RpcAuth cred, GenItfXdrTransport<GenOncRpcSvc> transport) {
        super(prog, ver, cred, new Xdr(Xdr.INITIAL_XDR_SIZE), transport);
    }

    public GenRpcCall(int prog, int ver, RpcAuth cred, Xdr xdr, GenXdrTransport<GenOncRpcSvc> transport) {
        super(prog, ver, cred, xdr, transport);
    }

    public GenRpcCall(int xid, Xdr xdr, GenXdrTransport<GenOncRpcSvc> transport) {
        super(xid,xdr,transport);
    }

    public GenRpcCall(int xid, int prog, int ver, int proc, RpcAuth cred, Xdr xdr, GenXdrTransport<GenOncRpcSvc> transport) {
        super(xid,prog,ver,proc,cred,xdr,transport);
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfRpcCall#accept()
     */
    @Override
    public void accept() throws IOException, OncRpcException {
         _rpcvers = _xdr.xdrDecodeInt();
         if (_rpcvers != RPCVERS) {
            throw new RpcMismatchReply(_rpcvers, 2);
         }

        _prog = _xdr.xdrDecodeInt();
        _version = _xdr.xdrDecodeInt();
        _proc = _xdr.xdrDecodeInt();
        _cred = RpcCredential.decode(_xdr);
     }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfRpcCall#toString()
     */
    @Override
    public String toString() {
        return String.format("RPCv%d call: program=%d, version=%d, procedure=%d",
                _rpcvers, _prog, _version, _proc);
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfRpcCall#acceptedReply(int, org.dcache.xdr.XdrAble)
     */
    @Override
    public void acceptedReply(int state, XdrAble reply) {

        XdrEncodingStream xdr = _xdr;
        try {
            RpcMessage replyMessage = new RpcMessage(_xid,RpcMessageType.REPLY);
            xdr.beginEncoding();
            replyMessage.xdrEncode(_xdr);
            xdr.xdrEncodeInt(RpcReplyStatus.MSG_ACCEPTED);
            _cred.getVerifier().xdrEncode(xdr);
            xdr.xdrEncodeInt(state);
            reply.xdrEncode(xdr);
            xdr.endEncoding();

            _transport.send((Xdr)xdr, _transport.getRemoteSocketAddress(), _sendNotificationHandler);

        } catch (OncRpcException e) {
            _log.warn("Xdr exception: ", e);
        } catch (IOException e) {
            _log.error("Failed send reply: ", e);
        }
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfRpcCall#failProgramMismatch(int, int)
     */
    @Override
    public void failProgramMismatch(int min, int max) {
        acceptedReply(RpcAccepsStatus.PROG_MISMATCH, new MismatchInfo(min, max));
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfRpcCall#failProgramUnavailable()
     */
    @Override
    public void failProgramUnavailable() {
        acceptedReply(RpcAccepsStatus.PROG_UNAVAIL, XdrVoid.XDR_VOID);
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfRpcCall#failProcedureUnavailable()
     */
    @Override
    public void failProcedureUnavailable() {
        acceptedReply(RpcAccepsStatus.PROC_UNAVAIL, XdrVoid.XDR_VOID);
    }

    /**
     * executes an RPC. returns the (internally generated) xid for the call
     * @param procedure The number of the procedure.
     * @param args The argument of the procedure.
     * @param callback The completion handler.
     * @param timeoutValue timeout value. 0 means no timeout
     * @param timeoutUnits units for timeout value
     * @param auth auth to use for this call. null for constructor-provided default
     * @return the xid for the call
     * @throws OncRpcException
     * @throws IOException
     */
    @Override
    protected int callInternal(int procedure, XdrAble args,
            CompletionHandler<GenItfRpcReply<GenOncRpcSvc>, GenItfXdrTransport<GenOncRpcSvc>> callback,
                             long timeoutValue, TimeUnit timeoutUnits, RpcAuth auth)
            throws IOException {

        int xid = nextXid();

        Xdr xdr = new Xdr(Xdr.INITIAL_XDR_SIZE);
        xdr.beginEncoding();
        RpcMessage rpcMessage = GenAbstractRpcCall.createMessage(xid, RpcMessageType.CALL);
        rpcMessage.xdrEncode(xdr);
        xdr.xdrEncodeInt(RPCVERS);
        xdr.xdrEncodeInt(_prog);
        xdr.xdrEncodeInt(_version);
        xdr.xdrEncodeInt(procedure);
        if (auth != null) {
            auth.xdrEncode(xdr);
        } else {
            _cred.xdrEncode(xdr);
        }
        args.xdrEncode(xdr);
        xdr.endEncoding();

        GenItfReplyQueue<GenOncRpcSvc> replyQueue = _transport.getReplyQueue();
        if (callback != null) {
            replyQueue.registerKey(xid, _transport.getLocalSocketAddress(), callback, timeoutValue, timeoutUnits);
        } else {
            //no handler, so we wont get any errors if connection was dropped. have to check.
            if (!_transport.isOpen()) {
                throw new EOFException("XdrTransport is not open");
            }
        }

        _transport.send(xdr, _transport.getRemoteSocketAddress(), new NotifyListenersCompletionHandler() {

            @Override
            public void failed(Throwable t, InetSocketAddress attachment) {
                super.failed(t, attachment);
                if (callback != null) {
                    replyQueue.get(xid);
                    callback.failed(t, _transport);
                }
            }
        });
        return xid;
    }
}
