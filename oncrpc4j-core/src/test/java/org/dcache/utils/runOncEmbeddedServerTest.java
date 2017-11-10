package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.GenOncRpcSvc;

public class runOncEmbeddedServerTest extends runGenericEmbeddedServerTest<GenOncRpcSvc,EmbeddedOncServer> {
    @Override
    protected EmbeddedOncServer createEmbededServer(int port) throws IOException {
        return new EmbeddedOncServer(0);
    }
}
