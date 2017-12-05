package org.dcache.xdr;

import org.dcache.xdr.gss.GssSessionManager;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;

public interface IOncRpcSvcBuilder extends OncRpcSvcBuilderItf<IOncRpcSvc,IOncRpcSvcBuilder> {
    IOncRpcSvcBuilder withGssSessionManager(GssSessionManager gssSessionManager);
    GssSessionManager getGssSessionManager();
    IOncRpcSvcBuilder withIpProtocolType(int protocol);
}
