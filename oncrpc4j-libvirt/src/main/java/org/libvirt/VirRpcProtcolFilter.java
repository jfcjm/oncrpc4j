package org.libvirt;

import java.io.IOException;

import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.RpcProtocolFilterUtf;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

public interface VirRpcProtcolFilter extends RpcProtocolFilterUtf<VirRpcSvc>{
    @Override
    NextAction handleRead(FilterChainContext ctx) throws IOException;

    static RpcProtocolFilterUtf<VirRpcSvc> getImpl(ReplyQueueItf<VirRpcSvc> replyQueue) {
        return new VirRpcProtocolFilterImpl(replyQueue);
    }

}