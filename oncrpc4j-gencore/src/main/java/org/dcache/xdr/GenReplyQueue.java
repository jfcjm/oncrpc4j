package org.dcache.xdr;


import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.root.AbstractReplyQueue;

public class GenReplyQueue 
    extends AbstractReplyQueue<GenOncRpcSvc,GenOncRpcCall,XdrTransport,GenOncRpcReply> 
    implements ReplyQueueItf<GenOncRpcSvc,GenOncRpcCall,XdrTransport,GenOncRpcReply> {


}
