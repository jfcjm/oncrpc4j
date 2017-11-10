package org.dcache.xdr.model.root;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.dcache.xdr.BadXdrOncRpcException;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAccepsStatus;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcMessageType;
import org.dcache.xdr.RpcReplyStatus;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.xdr.XdrVoid;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

public abstract class GenAbstractRpcCall<SVC_T extends RpcSvcItf<SVC_T>>  implements RpcCallItf<SVC_T>{

    final static Logger _log = LoggerFactory.getLogger(GenAbstractRpcCall.class);
    //TODO : fin a better way for RpcMessage Constructor
    protected static RpcMessage createMessage(int xid,int messagetype){
        return new RpcMessage(xid, messagetype);
    }
    
    protected static RpcMessage createMessage(Xdr xdr) throws BadXdrOncRpcException{
        return new RpcMessage(xdr);
    }
    
    
    private final static Random RND = new Random();

    /**
     * XID number generator
     */
    final AtomicInteger xidGenerator = new AtomicInteger(RND.nextInt());

    protected int _xid;

    /**
     * Supported RPC protocol version
     */
    protected final static int RPCVERS = 2;

    /**
     * RPC program number
     */
    protected int _prog;

    /**
     * RPC program version number
     */
    protected int _version;

    /**
     * RPC program procedure number
     */
    protected int _proc;

    /**
     *  RPC protocol version number
     */
    protected int _rpcvers;

    /**
     * Authentication credential.
     */
    protected RpcAuth _cred;

    /**
     * RPC call transport.
     */
    protected final XdrTransportItf<SVC_T> _transport;

    /**
     * Call body.
     */
    protected final Xdr _xdr;

    /**
     * Object used to synchronize access to sendListeners.
     */
    final Object _listenerLock = new Object();

    /**
     * The {link CompletionHandler} which is used to notify all registered
     * completion listeners.
     */

    protected final CompletionHandler<Integer, InetSocketAddress> _sendNotificationHandler
            = new NotifyListenersCompletionHandler();


    /**
     * A {@link List} of registered {@link CompletionHandler} to be notified when
     * send request complete.
     */
    List<CompletionHandler<Integer, InetSocketAddress>> _sendListeners;

    /**
     * A {@link List} of registered {@link CompletionHandler} to be notified
     * when send request complete. The listeners will be removed from the list
     * after notification.
     */
    List<CompletionHandler<Integer, InetSocketAddress>> _sendOnceListeners;
    
    public GenAbstractRpcCall(int prog, int ver, RpcAuth cred, XdrTransportItf<SVC_T> transport) {
        this(prog, ver, cred, new Xdr(Xdr.INITIAL_XDR_SIZE), transport);
    }

    public GenAbstractRpcCall(int prog, int ver, RpcAuth cred, Xdr xdr, XdrTransportItf<SVC_T> transport) {
        _prog = prog;
        _version = ver;
        _cred = cred;
        _transport = transport;
        _xdr = xdr;
        _proc = 0;
    }

    public GenAbstractRpcCall(int xid, Xdr xdr, XdrTransportItf<SVC_T> transport) {
        _xid = xid;
        _xdr = xdr;
        _transport = transport;
    }

    public GenAbstractRpcCall(int xid, int prog, int ver, int proc, RpcAuth cred, Xdr xdr, XdrTransportItf<SVC_T> transport) {
        _xid = xid;
        _prog = prog;
        _version = ver;
        _proc = proc;
        _cred = cred;
        _xdr = xdr;
        _transport = transport;
        _rpcvers = RPCVERS;
    }
    

    
    protected abstract int callInternal(int procedure, XdrAble args,
            CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback, long timeoutValue,
            TimeUnit timeoutUnits, RpcAuth auth) throws IOException;
    
    
    @Override
    public int getProgram() {
        return _prog;
    }

    @Override
    public int getProgramVersion() {
        return _version;
    }

    @Override
    public int getProcedure() {
        return _proc;
    }

    @Override
    public RpcAuth getCredential() {
        return _cred;
    }

    @Override
    public XdrTransportItf<SVC_T> getTransport() {
        return _transport;
    }

    @Override
    public int getXid() {
        return _xid;
    }

    @Override
    public Xdr getXdr() {
        return _xdr;
    }

    @Override
    public void reject(int status, XdrAble reason) {

        XdrEncodingStream xdr = _xdr;
        try {
            RpcMessage replyMessage = new RpcMessage(_xid, RpcMessageType.REPLY);
            
            xdr.beginEncoding();
            replyMessage.xdrEncode(xdr);
           
            replyMessage.xdrEncode(_xdr);
            xdr.xdrEncodeInt(RpcReplyStatus.MSG_DENIED);
            xdr.xdrEncodeInt(status);
            reason.xdrEncode(_xdr);
            
            xdr.endEncoding();
    
            _transport.send((Xdr)xdr, _transport.getRemoteSocketAddress(), _sendNotificationHandler);
    
        } catch (OncRpcException e) {
            _log.warn("Xdr exception: ", e);
        } catch (IOException e) {
            _log.error("Failed send reply: ", e);
        }
    }

    @Override
    public void reply(XdrAble reply) {
        acceptedReply(RpcAccepsStatus.SUCCESS, reply);
    }

    @Override
    public void retrieveCall(XdrAble args) throws OncRpcException, IOException {
        args.xdrDecode(_xdr);
        _xdr.endDecoding();
    }

    @Override
    public void failRpcGarbage() {
        acceptedReply(RpcAccepsStatus.GARBAGE_ARGS, XdrVoid.XDR_VOID);
    }

    @Override
    public void failRpcSystem() {
        acceptedReply(RpcAccepsStatus.SYSTEM, XdrVoid.XDR_VOID);
    }

    @Override
    public void call(int procedure, XdrAble args, CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback, long timeoutValue, TimeUnit timeoutUnits, RpcAuth auth)
            throws IOException {
                callInternal(procedure, args, callback, timeoutValue, timeoutUnits, auth);
            }

    @Override
    public void call(int procedure, XdrAble args, CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback, long timeoutValue, TimeUnit timeoutUnits) throws IOException {
        callInternal(procedure, args, callback, timeoutValue, timeoutUnits, null);
    }

    @Override
    public void call(int procedure, XdrAble args, CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback, RpcAuth auth) throws IOException {
        callInternal(procedure, args, callback, 0, null, auth);
    }

    @Override
    public void call(int procedure, XdrAble args, CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback) throws IOException {
        callInternal(procedure, args, callback, 0, null, null);
    }

    @Override
    public <T extends XdrAble> Future<T> call(int procedure, XdrAble args, final Class<T> type, final RpcAuth auth) throws IOException {
        try {
            T result = type.getDeclaredConstructor().newInstance();
            return getCallFuture(procedure, args, result, 0, null, auth);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            // this exceptions point to bugs
            throw new RuntimeException("Failed to create in instance of " + type, e);
        }
    }

    @Override
    public <T extends XdrAble> Future<T> call(int procedure, XdrAble args, final Class<T> type) throws IOException {
        return call(procedure, args, type, null);
    }

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
                    Throwables.throwIfInstanceOf(t, OncRpcException.class);
                    Throwables.throwIfInstanceOf(t, IOException.class);
                    Throwables.throwIfInstanceOf(t, TimeoutException.class);
                    throw new IOException(t);
                }
            }

    @Override
    public void call(int procedure, XdrAble args, XdrAble result, long timeoutValue, TimeUnit timeoutUnits)
            throws IOException, TimeoutException {
                call(procedure, args, result, timeoutValue, timeoutUnits, null);
            }

    @Override
    public void call(int procedure, XdrAble args, XdrAble result, RpcAuth auth) throws IOException {
        try {
            call(procedure, args, result, 0, null, auth);
        } catch (TimeoutException e) {
            throw new IllegalStateException(e); //theoretically impossible
        }
    }

    @Override
    public void call(int procedure, XdrAble args, XdrAble result) throws IOException {
        try {
            call(procedure, args, result, 0, null, null);
        } catch (TimeoutException e) {
            throw new IllegalStateException(e); //theoretically impossible
        }
    }

    protected <T extends XdrAble> Future<T> getCallFuture(int procedure, XdrAble args, final T result, long timeoutValue, TimeUnit timeoutUnits,
            RpcAuth auth) throws IOException {
            
                final CompletableFuture<T> future = new CompletableFuture<>();
                CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback = new CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>>() {
            
                    @Override
                    public void completed(RpcReplyItf<SVC_T> reply, XdrTransportItf<SVC_T> attachment) {
                        try {
                            reply.getReplyResult(result);
                            future.complete(result);
                        } catch (IOException e) {
                            failed(e, attachment);
                        }
                    }
            
                    @Override
                    public void failed(Throwable exc, XdrTransportItf<SVC_T> attachment) {
                        future.completeExceptionally(exc);
                    }
                };
            
                int xid = callInternal(procedure, args, callback, timeoutValue, timeoutUnits, auth);
                //wrap the future if no timeout provided up-front to properly un-register
                //the handler if a timeout is later provided to Future.get()
                return timeoutValue > 0 ? future : new TimeoutAwareFuture<>(future, xid);
            }

    protected int nextXid() {
        return xidGenerator.incrementAndGet();
    }

    @Override
    public void registerSendListener(CompletionHandler<Integer, InetSocketAddress> listener) {
        synchronized (_listenerLock) {
            if (_sendListeners == null) {
                _sendListeners = new ArrayList<>();
            }
            _sendListeners.add(listener);
        }
    }

    @Override
    public void registerSendOnceListener(CompletionHandler<Integer, InetSocketAddress> listener) {
        synchronized (_listenerLock) {
            if (_sendOnceListeners == null) {
                _sendOnceListeners = new ArrayList<>();
            }
            _sendOnceListeners.add(listener);
        }
    }


    /**
     * The {link CompletionHandler} which is used to notify all registered
     * completion listeners.
     */
    protected class NotifyListenersCompletionHandler implements CompletionHandler<Integer, InetSocketAddress> {

        @Override
        public void completed(Integer result, InetSocketAddress attachment) {
            synchronized (_listenerLock) {
                if (_sendListeners != null) {
                    _sendListeners
                            .parallelStream()
                            .forEach(l -> l.completed(result, attachment));
                }

                if (_sendOnceListeners != null) {
                    _sendOnceListeners
                            .parallelStream()
                            .forEach(l -> l.completed(result, attachment));
                    _sendOnceListeners = null;
                }
            }
        }

        @Override
        public void failed(Throwable t, InetSocketAddress attachment) {
            _log.error("Failed to send RPC to {} : {}", attachment, t.getMessage());
            synchronized (_listenerLock) {
                if (_sendListeners != null) {
                    _sendListeners
                            .parallelStream()
                            .forEach(l -> l.failed(t, attachment));
                }

                if (_sendOnceListeners != null) {
                    _sendOnceListeners
                            .parallelStream()
                            .forEach(l -> l.failed(t, attachment));
                    _sendOnceListeners = null;
                }
            }
        }
    }

    class TimeoutAwareFuture<T> implements Future<T> {
        private final Future<T> delegate;
        private final int xid;

        public TimeoutAwareFuture(Future<T> delegate, int xid) {
            this.delegate = delegate;
            this.xid = xid;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            try {
                return delegate.cancel(mayInterruptIfRunning);
            } finally {
                if (mayInterruptIfRunning) {
                    unregisterXid();
                }
            }
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public boolean isDone() {
            return delegate.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            try {
                return delegate.get();
            } finally {
                unregisterXid();
            }
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                return delegate.get(timeout, unit);
            } finally {
                unregisterXid();
            }
        }

        private void unregisterXid() {
            _transport.getReplyQueue().get(xid); //make sure its removed from the reply queue
        }
    }
}