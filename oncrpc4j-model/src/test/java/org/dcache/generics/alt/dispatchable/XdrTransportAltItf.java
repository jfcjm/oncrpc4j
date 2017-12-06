package org.dcache.generics.alt.dispatchable;

import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;

import org.dcache.xdr.Xdr;
import org.dcache.xdr.model.itf.ReplyQueueItf;

public interface XdrTransportAltItf<SVC_T extends RpcSvcAltItf<SVC_T,CALL_T>,CALL_T extends RpcCallAltItf<SVC_T,CALL_T>> {

    /**
     * Send data to remote end point. The handler parameter is a completion
     * handler that is invoked when the send operation completes (or fails). The
     * result passed to the completion handler is the number of bytes sent.
     *
     * @param <A> the type of the attachment.
     * @param xdr message to send.
     * @param attachment the object to attach to the I/O operation; can be null
     * @param handler the handler for consuming the result.
     */
    <A> void send(Xdr xdr, A attachment, CompletionHandler<Integer, ? super A> handler);

    ReplyQueueAltItf<SVC_T,CALL_T> getReplyQueue();

    /**
     * Returns is this transport is open and ready.
     *
     * @return <tt>true</tt>, if connection is open and ready, or <tt>false</tt>
     * otherwise.
     */
    boolean isOpen();

    /**
     * Get local end point.
     *
     * @return InetSocketAddress of local socket end point
     */
    InetSocketAddress getLocalSocketAddress();

    /**
     * Get remote end point.
     *
     * @return InetSocketAddress of remote socket end point.
     */
    InetSocketAddress getRemoteSocketAddress();

    /**
     * Get {@link XdrTransport} for to sent/receive requests in opposite direction.
     * The returned transport can be used by servers to send rpc calls to clients and
     * can be used by clients to receive rpc calls from servers.
     *
     * @return
     */
    XdrTransportAltItf<SVC_T,CALL_T> getPeerTransport();

    //TODO ProtocolFactoryItf<SVC_T> getProtocolFactory();
    
    

}