package org.dcache.generics.alt;

import java.net.InetSocketAddress;

import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.root.AbstractOncRpcSvc;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Transport;

public class OncSvcAlt extends AbstractOncRpcSvc<OncSvcAlt,OncSvcBuilderAlt>{

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

}
