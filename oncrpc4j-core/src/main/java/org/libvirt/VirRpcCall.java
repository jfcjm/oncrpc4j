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
package org.libvirt;

import com.google.common.base.Throwables;
import java.io.EOFException;

import org.dcache.xdr.MismatchInfo;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.ReplyQueue;
import org.dcache.xdr.RpcAccepsStatus;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.RpcCredential;
import org.dcache.xdr.RpcMessage;
import org.dcache.xdr.RpcMessageType;
import org.dcache.xdr.RpcMismatchReply;
import org.dcache.xdr.RpcReply;
import org.dcache.xdr.RpcReplyStatus;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.XdrVoid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
/*
 * Apdaptation of rpcCall for libvirt ! does not use rpcvers nor
 * rpcauth
 */
public class VirRpcCall extends RpcCall{

    private final static Logger _log = LoggerFactory.getLogger(VirRpcCall.class);

    public VirRpcCall(int prog, int ver, RpcAuth cred, XdrTransport transport) {
        this(prog, ver, cred, new Xdr(Xdr.INITIAL_XDR_SIZE), transport);
    }

    public VirRpcCall(int prog, int ver, RpcAuth cred, Xdr xdr, XdrTransport transport) {
        super(prog,ver,null,xdr,transport);
        
    }

    public VirRpcCall(int xid, Xdr xdr, XdrTransport transport) {
        super(xid,xdr,transport);
    }

    public VirRpcCall(int xid, int prog, int ver, int proc, RpcAuth cred, Xdr xdr, XdrTransport transport) {
        super(xid,prog,ver,proc,null,xdr,transport);
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
    protected int callInternal(int procedure, XdrAble args, CompletionHandler<RpcReply, XdrTransport> callback,
                             long timeoutValue, TimeUnit timeoutUnits, RpcAuth auth)
            throws IOException {

        int xid = nextXid();
        
        Xdr xdr = new Xdr(Xdr.INITIAL_XDR_SIZE);
        xdr.beginEncoding();
        //RpcMessage rpcMessage = new RpcMessage(xid, RpcMessageType.CALL);
        //rpcMessage.xdrEncode(xdr);
        //xdr.xdrEncodeInt(RPCVERS);
        xdr.xdrEncodeInt(getProgram());
        xdr.xdrEncodeInt(getProgramVersion());
        xdr.xdrEncodeInt(procedure);
        if (auth != null) {
            auth.xdrEncode(xdr);
        } else {
            //_cred.xdrEncode(xdr);
        }
        xdr.xdrEncodeInt(0); //type
        xdr.xdrEncodeInt(xid);//serial
        xdr.xdrEncodeInt(0);//status
        args.xdrEncode(xdr);
        xdr.endEncoding();
        /*
        traité par filtre sur les connection (rpcmessageparszeer/rpcprotocolfilter
        Buffer buf = xdr.asBuffer();
        xdr.xdrEncodeInt(xdr.asBuffer().remaining());
        buf.rewind();
        */
        _log.debug("extended :xid {}", xid);
        System.out.println("wrote " + xdr.asBuffer().limit() + "bytes");
        System.out.println("remaining " + xdr.asBuffer().remaining() + "bytes");
        System.out.println("first " + xdr.asBuffer().array()[0]);
        System.out.println("first " + xdr.asBuffer().array()[1]);
        System.out.println("first " + xdr.asBuffer().array()[2]);
        System.out.println("first " + xdr.asBuffer().array()[3]);
        
        
        XdrTransport _transport = getTransport();
        
        ReplyQueue replyQueue = _transport.getReplyQueue();
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
    @Override
    public void accept() throws IOException, OncRpcException {
        /*_rpcvers = _xdr.xdrDecodeInt();
        if (_rpcvers != RPCVERS) {
           throw new RpcMismatchReply(_rpcvers, 2);
        }
        */
       _prog = _xdr.xdrDecodeInt();
       _version = _xdr.xdrDecodeInt();
       _proc = _xdr.xdrDecodeInt();
       //_cred = RpcCredential.decode(_xdr);
    }
    
}
