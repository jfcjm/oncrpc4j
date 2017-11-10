package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.OncRpcSvc;

public class runOncEmbeddedServerTest extends runGenericEmbeddedServerTest<OncRpcSvc,EmbeddedGenOncServer> {
    @Override
    protected EmbeddedGenOncServer createEmbededServer(int port) throws IOException {
        return new EmbeddedGenOncServer(0);
    }
}
