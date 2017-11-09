package org.dcache.utils;

import java.io.IOException;

import org.libvirt.GenVirOncRpcSvc;


public class runVirEmbeddedServerTest 
            extends runGenericEmbeddedServerTest<GenVirOncRpcSvc,EmbeddedGenVirtServer> {

    
    @Override
    protected EmbeddedGenVirtServer createEmbededServer(int port) throws IOException {
        return new EmbeddedGenVirtServer(0);
    }
   /*
    @Override
    protected EmbeddedGenericServer<GenRpcSvc> createEmbededServer(int port) throws IOException {
        return new EmbeddedGenVirtServer(0);
    }
    */
    /*
    @Override
    protected EmbeddedGenVirtServer createEmbededServer(int port) throws IOException {
        return new EmbeddedGenVirtServer(0);
    }
   */
}
