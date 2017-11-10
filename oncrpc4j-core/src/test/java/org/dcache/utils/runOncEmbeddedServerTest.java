package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.GenOncRpcSvc;

public class runOncEmbeddedServerTest extends runGenericEmbeddedServerTest<GenOncRpcSvc,EmbeddedGenOncServer> {
    @Override
    protected EmbeddedGenOncServer createEmbededServer(int port) throws IOException {
        return new EmbeddedGenOncServer(0);
    }
}
