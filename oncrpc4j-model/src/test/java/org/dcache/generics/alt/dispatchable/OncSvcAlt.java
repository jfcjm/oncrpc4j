package org.dcache.generics.alt.dispatchable;

import java.net.InetSocketAddress;

import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.root.AbstractOncRpcSvc;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.filterchain.Filter;

public class OncSvcAlt extends AbstractOncRpcSvc<OncSvcAlt,RpcCallAlt,OncSvcBuilderAlt>{

    boolean _prestartCalled;
    boolean _prestopCalled;
    boolean _hasDummyOption;

    public OncSvcAlt(OncSvcBuilderAlt builder) {
        super(builder);
    }

    @Override
    protected OncSvcAlt getThis() {
        return this;
    }

    @Override
    protected void doPreStartAction(Connection<InetSocketAddress> c) {
        _prestartCalled = true;
        
    }

    @Override
    protected void preStopActions() {
        _prestopCalled = true;
        
    }

    @Override
    protected void processBuilder(OncSvcBuilderAlt builder) {
        _hasDummyOption =  builder.hasDummyOption();
    }

    @Override
    protected Filter createRpcProtocolFilter(ReplyQueueItf<OncSvcAlt, RpcCallAlt> _replyQueue) {
        return null;
    }

    @Override
    protected ReplyQueueItf<OncSvcAlt, RpcCallAlt> createReplyQueue() {
        return null;
    }

}
