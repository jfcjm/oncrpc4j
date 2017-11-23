package org.dcache.utils;

import java.io.IOException;

import org.libvirt.VirRpcSvc;


public class runVirEmbeddedServerTest extends runGenericEmbeddedServerTest<VirRpcSvc> {
    
    
    private static final EmbeddedGenericServerFactory<VirRpcSvc> factory = 
            new EmbeddedGenericServerFactory<VirRpcSvc>(){
                @Override
                public EmbeddedGenericServer<VirRpcSvc> createEmbeddedServer(int port) throws IOException {
                    return new EmbeddedGenVirtServer(0);
                }
        
    };

    public runVirEmbeddedServerTest() {
        super(factory );
    }
}
