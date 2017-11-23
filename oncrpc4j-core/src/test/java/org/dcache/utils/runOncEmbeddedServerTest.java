package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.OncRpcSvc;

public class runOncEmbeddedServerTest extends runGenericEmbeddedServerTest<OncRpcSvc> {
    
    
    
    private static final EmbeddedGenericServerFactory<OncRpcSvc> factory = 
            new EmbeddedGenericServerFactory<OncRpcSvc>(){
                @Override
                public EmbeddedGenericServer<OncRpcSvc> createEmbeddedServer(int port) throws IOException {
                    return new EmbeddedGenOncServer(0);
                }
        
    };

    public runOncEmbeddedServerTest() {
        super(factory );
    }
}
