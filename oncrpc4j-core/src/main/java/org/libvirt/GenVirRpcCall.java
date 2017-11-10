/*******************************************************************************
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

import java.io.EOFException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcMessageType;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.xdr.model.itf.GenItfReplyQueue;
import org.dcache.xdr.model.itf.GenItfRpcReply;
import org.dcache.xdr.model.itf.GenItfXdrTransport;
import org.dcache.xdr.model.root.GenAbstractRpcCall;
import org.dcache.xdr.model.root.RpcMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/*
 * Apdaptation of rpcCall for libvirt ! does not use rpcvers nor
 * rpcauth
 */
public final class GenVirRpcCall extends GenAbstractRpcCall<GenVirOncRpcSvc> implements GenItfVirtRpcCall{

    private final static Logger _log = LoggerFactory.getLogger(GenVirRpcCall.class);
    private int _status;
    /**
     * Construit un RPC libvirt
     * @param prog      numero du programme
     * @param ver       veriosn du programme
     * @param transport XdrTransport to use
     */
    public GenVirRpcCall(int prog, int ver,  GenItfXdrTransport<GenVirOncRpcSvc> transport) {
        this(prog, ver,  new Xdr(Xdr.INITIAL_XDR_SIZE), transport);
    }

    public GenVirRpcCall(int prog, int ver,  Xdr xdr, GenItfXdrTransport<GenVirOncRpcSvc> transport) {
        super(prog,ver,null,xdr,transport);
        
    }

    public GenVirRpcCall(int xid, Xdr xdr, GenItfXdrTransport<GenVirOncRpcSvc> transport) {
        super(xid,xdr,transport);
    }

    public GenVirRpcCall(int xid, int prog, int ver, int proc, Xdr xdr, GenItfXdrTransport<GenVirOncRpcSvc> transport) {
        super(xid,prog,ver,proc,null,xdr,transport);
    }
    /* (non-Javadoc)
     * @see org.libvirt.GenItfVirtRpcCall#call(int, org.dcache.xdr.XdrAble, org.dcache.xdr.XdrAble, long, java.util.concurrent.TimeUnit, org.dcache.xdr.RpcAuth)
     */
    @Override
    public void call(int procedure, XdrAble args, XdrAble result, long timeoutValue, TimeUnit timeoutUnits, RpcAuth auth)
            throws IOException, TimeoutException {
        try {
            Future<XdrAble> future = getCallFuture(procedure, args, result, timeoutValue, timeoutUnits, auth);
            future.get();
        } catch (InterruptedException e) {
            // workaround missing chained constructor
            IOException ioe = new InterruptedIOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } catch (ExecutionException e) {
            Throwable t = Throwables.getRootCause(e);
            _log.warn("error " + t);
            /*
            Throwables.throwIfInstanceOf(t, OncRpcException.class);
            Throwables.throwIfInstanceOf(t, IOException.class);
            Throwables.throwIfInstanceOf(t, TimeoutException.class);
            */
            {
           
                Throwable clientException;
                try {
                    System.out.println(t.getClass());
                    clientException = t.getClass().newInstance();
                    clientException.initCause(t);
                    if (t instanceof OncRpcException) throw (OncRpcException) clientException;
                    if (t instanceof IOException) throw (IOException) clientException;
                    if (t instanceof TimeoutException) throw (TimeoutException) clientException;
                   
                } catch (InstantiationException | IllegalAccessException inner) {
                    throw new IOException(inner);
                } 
            }
            throw new IOException(t);
        }
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
            CompletionHandler<GenItfRpcReply<GenVirOncRpcSvc>, GenItfXdrTransport<GenVirOncRpcSvc>> callback,
                             long timeoutValue, TimeUnit timeoutUnits, RpcAuth auth)
            throws IOException {
        int xid = nextXid();
        _log.debug("calling procedure {} with args {} (xid: {})",procedure,args.toString(),xid);
        
        Xdr xdr = new Xdr(Xdr.INITIAL_XDR_SIZE);
        RpcMessage rpcMessage = new VirRpcMessage(xid,RpcMessageType.CALL);
        xdr.beginEncoding();
        xdr.xdrEncodeInt(getProgram());
        xdr.xdrEncodeInt(getProgramVersion());
        xdr.xdrEncodeInt(procedure);
        rpcMessage.xdrEncode(xdr);
        xdr.xdrEncodeInt(0);//status
        args.xdrEncode(xdr);
        xdr.endEncoding();
        _log.debug("extended :xid {}", xid);
        System.out.println("wrote " + xdr.asBuffer().limit() + "bytes");
        
        
        GenItfXdrTransport<GenVirOncRpcSvc> _transport = getTransport();
        
        GenItfReplyQueue<GenVirOncRpcSvc> replyQueue = _transport.getReplyQueue();
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
    /* (non-Javadoc)
     * @see org.libvirt.GenItfVirtRpcCall#accept()
     */
    @Override
    public void accept() throws IOException, OncRpcException {
        _log.debug("Before remaining {} ",_xdr.asBuffer().remaining());
       _prog = _xdr.xdrDecodeInt();
       _version = _xdr.xdrDecodeInt();
       _proc = _xdr.xdrDecodeInt();
       {
           //fake Read of type and xid
           _xdr.xdrDecodeInt();//type
           _xdr.xdrDecodeInt();//xid
       }
       _status = _xdr.xdrDecodeInt();//status
       if (0 != _status) {
           _log.warn("status is not equal to zero : {}",_status);
           throw new  VirRpcException("status should be equal to 0 see https://github.com/libvirt/libvirt/blob/master/src/rpc/virnetprotocol.x");
       }
       _log.info("Accepted call for prog {}, version {} and proc {}, status {}",_prog,_version,_proc,_status);
       _log.debug("{} byte(s) are remaining in the buffer",_xdr.asBuffer().remaining());
    }
    /* (non-Javadoc)
     * @see org.libvirt.GenItfVirtRpcCall#acceptedReply(int, org.dcache.xdr.XdrAble)
     */
    @Override
    public void acceptedReply(int state, XdrAble reply) {
        _log.info("Construct a reply for state {} and reply <{}> (program {}, version {}, proc {})", state,reply,getProgram(),getProgramVersion(),getProcedure());
        XdrEncodingStream xdr = _xdr;
        try {
            RpcMessage replyMessage = new VirRpcMessage(getXid(), RpcMessageType.REPLY);
            xdr.beginEncoding();
            xdr.xdrEncodeInt(getProgram());
            xdr.xdrEncodeInt(getProgramVersion());
            xdr.xdrEncodeInt(getProcedure());
            replyMessage.xdrEncode(_xdr);
            xdr.xdrEncodeInt(state);
            
            // No state for libvirt replies xdr.xdrEncodeInt(state);
            reply.xdrEncode(xdr);
            xdr.endEncoding();
            {
                GenItfXdrTransport<GenVirOncRpcSvc> _transport = getTransport();
                _transport.send((Xdr)xdr, _transport.getRemoteSocketAddress(), _sendNotificationHandler);
            }

        } catch (VirRpcException e) {
            _log.warn("Xdr exception: ", e);
        } catch (IOException e) {
            _log.error("Failed send reply: ", e);
        }
    }
    /* (non-Javadoc)
     * @see org.libvirt.GenItfVirtRpcCall#failProgramUnavailable()
     */
    @Override
    public  void failProgramUnavailable(){
        acceptedReply(VirRpcAcceptStatus.ERROR, GenVirError.createProgramUnavailable(this));
    }
    /* (non-Javadoc)
     * @see org.libvirt.GenItfVirtRpcCall#failProcedureUnavailable()
     */
    @Override
    public void failProcedureUnavailable() {
        acceptedReply(VirRpcAcceptStatus.ERROR, GenVirError.createProgramUnavailable(this));
    }



    /* (non-Javadoc)
     * @see org.libvirt.GenItfVirtRpcCall#failRuntimeError(java.lang.Exception)
     */
    @Override
    public void failRuntimeError(Exception e) {
        acceptedReply(VirRpcAcceptStatus.ERROR,GenVirError.createRuntimeError(this,e));
    }
    
    /* (non-Javadoc)
     * @see org.libvirt.GenItfVirtRpcCall#toString()
     */
    @Override
    public String toString() {
        return String.format("LibVirtRPC call: program=%d, version=%d, procedure=%d,arg length %d", _prog, _version, _proc,_xdr.asBuffer().remaining());
    }

    @Override
    public void failProgramMismatch(int min, int max) {
        failProcedureUnavailable();
    }
}
