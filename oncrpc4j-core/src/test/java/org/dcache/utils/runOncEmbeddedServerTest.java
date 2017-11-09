package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.GenRpcSvc;

public class runOncEmbeddedServerTest extends runGenericEmbeddedServerTest<FromGenOncRpcSvc,EmbeddedOncServer> {
    @Override
    protected EmbeddedOncServer createEmbededServer(int port) throws IOException {
        return new EmbeddedOncServer(0);
    }
}
