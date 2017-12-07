package org.dcache.xdr;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.root.AbstractRpcCall;

public class RpcCall extends AbstractRpcCall<OncRpcSvc, RpcCall,XdrTransport,RpcReply> {


    public RpcCall(int i, int j, RpcAuth auth,XdrTransport transport) {
        super(i,j,auth,transport);
    }
    
    public RpcCall(HeaderItf<OncRpcSvc,RpcCall, XdrTransport,RpcReply> header, Xdr _xdr, XdrTransport transport) {
        super(header,_xdr,transport);
    }
    /*
    @Override
    public void call(int put1, XdrAble args$, CompletionHandler<RpcReply, XdrTransport> completionHandler,
            long _timeoutValue, TimeUnit _timeoutUnit, RpcAuth _auth) {
        CompletionHandler<RpcReplyItf<OncRpcSvc, RpcCall, XdrTransport,RpcReply>,XdrTransportItf<OncRpcSvc, RpcCall, XdrTransport,RpcReply>> ch = 
                new CompletionHandler<RpcReplyItf<OncRpcSvc, RpcCall, XdrTransport,RpcReply>,XdrTransportItf<OncRpcSvc, RpcCall, XdrTransport,RpcReply>> (){


                    @Override
                    public void completed(RpcReplyItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> result,
                            XdrTransportItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> attachment) {
                        RpcReply r = result.getThis();
                        XdrTransport a = attachment.getThis();
                        completionHandler.completed(r, a); 
                        
                        
                    }

                    @Override
                    public void failed(Throwable exc,
                            XdrTransportItf<OncRpcSvc, RpcCall, XdrTransport, RpcReply> attachment) {
                        XdrTransport a = attachment.getThis();
                        completionHandler.failed(exc, a);
                        
                    }

                    
                };
               
        super.call(put1,args$,ch,_timeoutValue,_timeoutUnit,_auth);
    }
    */

}
