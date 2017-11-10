package org.dcache.xdr.model.itf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcRejectStatus;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrAble;

public interface RpcCallItf<SVC_T extends RpcSvcItf<SVC_T>> {

    /**
     * Accept message. Have to be called prior processing RPC call.
     * @throws IOException
     * @throws OncRpcException
     */
    void accept() throws IOException, OncRpcException;

    /**
     * Get RPC call program number.
     *
     * @return version number
     */
    int getProgram();

    /**
     * @return the RPC call program version
     */
    int getProgramVersion();

    /**
     * @return the RPC call program procedure
     */
    int getProcedure();

    RpcAuth getCredential();

    /**
     * Get RPC {@XdrTransport} used by this call.
     * @return transport
     */
    XdrTransportItf<SVC_T> getTransport();

    /**
     * Get xid associated with this rpc message.
     */
    int getXid();

    /**
     * Get {@link Xdr} stream used by this message.
     * @return xdr stream
     */
    Xdr getXdr();

    String toString();

    /**
     * Reject the request with given status. The call can be rejected for two
     * reasons: either the server is not running a compatible version of the
     * RPC protocol (RPC_MISMATCH), or the server rejects the identity of the
     * caller (AUTH_ERROR).
     *
     * @see RpcRejectStatus
     * @param status
     * @param reason
     */
    void reject(int status, XdrAble reason);

    /**
     * Send accepted reply to the client.
     *
     * @param reply
     */
    void reply(XdrAble reply);

    void acceptedReply(int state, XdrAble reply);

    /**
     * Retrieves the parameters sent within an ONC/RPC call message.
     *
     * @param args the call argument do decode
     * @throws OncRpcException
     */
    void retrieveCall(XdrAble args) throws OncRpcException, IOException;

    /**
     * Reply to client with error program version mismatch.
     * Accepted message sent.
     *
     * @param min minimal supported version
     * @param max maximal supported version
     */
    void failProgramMismatch(int min, int max);

    /**
     * Reply to client with error program unavailable.
     * Accepted message sent.
     */
    void failProgramUnavailable();

    /**
     * Reply to client with error procedure unavailable.
     */
    void failProcedureUnavailable();

    /**
     * Reply to client with error garbage args.
     */
    void failRpcGarbage();

    /**
     * Reply to client with error system error.
     */
    void failRpcSystem();

    /**
     * Send asynchronous RPC request to a remove server.
     *
     * This method initiates an asynchronous RPC request. The handler parameter
     * is a completion handler that is invoked when the RPC operation completes
     * (or fails/times-out). The result passed to the completion handler is the
     * RPC result returned by server.
     *
     * @param procedure The number of the procedure.
     * @param args The argument of the procedure.
     * @param callback The completion handler.
     * @param timeoutValue timeout value. 0 means no timeout
     * @param timeoutUnits units for timeout value
     * @param auth auth to use for the call
     * @throws OncRpcException
     * @throws IOException
     * @since 2.4.0
     */
    void call(int procedure, XdrAble args, CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback,
            long timeoutValue, TimeUnit timeoutUnits, RpcAuth auth) throws IOException;

    /**
     * convenience version of {@link #call(int, XdrAble, CompletionHandler, long, TimeUnit, RpcAuth)} with no auth
     */
    void call(int procedure, XdrAble args, CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback,
            long timeoutValue, TimeUnit timeoutUnits) throws IOException;

    /**
     * convenience version of {@link #call(int, XdrAble, CompletionHandler, long, TimeUnit, RpcAuth)} with no timeout
     */
    void call(int procedure, XdrAble args, CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback,
            RpcAuth auth) throws IOException;

    /**
     * convenience version of {@link #call(int, XdrAble, CompletionHandler, long, TimeUnit, RpcAuth)} with no timeout or auth
     */
    void call(int procedure, XdrAble args, CompletionHandler<RpcReplyItf<SVC_T>, XdrTransportItf<SVC_T>> callback)
            throws IOException;

    /**
     * Send asynchronous RPC request to a remove server.
     *
     * This method initiates an asynchronous RPC request. The method behaves in
     * exactly the same manner as the {@link #call(int, XdrAble, CompletionHandler, long, TimeUnit)}
     * method except that instead of specifying a completion handler, this method
     * returns a Future representing the pending result. The Future's get method
     * returns the RPC reply responded by server.
     *
     * @param <T> The result type of RPC call.
     * @param procedure The number of the procedure.
     * @param args The argument of the procedure.
     * @param type The expected type of the reply
     * @param auth auth to use for the call
     * @return A Future representing the result of the operation.
     * @throws OncRpcException
     * @throws IOException
     * @since 2.4.0
     */
    <T extends XdrAble> Future<T> call(int procedure, XdrAble args, Class<T> type, RpcAuth auth) throws IOException;

    /**
     * convenience version of {@link #call(int, XdrAble, Class, RpcAuth)} with no auth
     */
    <T extends XdrAble> Future<T> call(int procedure, XdrAble args, Class<T> type) throws IOException;

    /**
     * Send call to remove RPC server.
     *
     * @param procedure the number of the procedure.
     * @param args the argument of the procedure.
     * @param result the result of the procedure
     * @param timeoutValue timeout value. 0 means no timeout
     * @param timeoutUnits units for timeout value
     * @param auth auth to use for the call
     * @throws OncRpcException
     * @throws IOException
     */
    void call(int procedure, XdrAble args, XdrAble result, long timeoutValue, TimeUnit timeoutUnits, RpcAuth auth)
            throws IOException, TimeoutException;

    /**
     * convenience version of {@link #call(int, XdrAble, XdrAble, long, TimeUnit, RpcAuth)} with default auth
     */
    void call(int procedure, XdrAble args, XdrAble result, long timeoutValue, TimeUnit timeoutUnits)
            throws IOException, TimeoutException;

    /**
     * convenience version of {@link #call(int, XdrAble, XdrAble, long, TimeUnit, RpcAuth)} with no timeout
     */
    void call(int procedure, XdrAble args, XdrAble result, RpcAuth auth) throws IOException;

    /**
     * convenience version of {@link #call(int, XdrAble, XdrAble, long, TimeUnit, RpcAuth)} with no timeout or auth
     */
    void call(int procedure, XdrAble args, XdrAble result) throws IOException;

    /**
     * Register {@link CompletionHandler} to receive notification when message
     * send is complete. NOTICE: when processing rpc call on the server side
     * the @{code registerSendListener} has the same effect as {@link #registerSendOnceListener}
     * as a new instance of {@link RpcCall} is used to process the request.
     * @param listener the message sent listener
     */
    void registerSendListener(CompletionHandler<Integer, InetSocketAddress> listener);

    /**
     * Register {@link CompletionHandler} to receive notification when message
     * send is complete. The listener will be removed after next send event.
     *
     * @param listener the message sent listener
     */
    void registerSendOnceListener(CompletionHandler<Integer, InetSocketAddress> listener);

}