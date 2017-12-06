package org.dcache.utils;

import static org.junit.Assert.*;

import java.io.IOException;

import org.dcache.xdr.OncRpcAcceptedException;
import org.dcache.xdr.XdrInt;
import org.dcache.xdr.XdrVoid;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.junit.Ignore;
import org.junit.Test;

public abstract class runGenericEmbeddedServerTest
    <
        SVC_T extends RpcSvcItf<SVC_T,CALL_T>, 
        CALL_T extends RpcCallItf<SVC_T,CALL_T>,
        BUILDER_T extends OncRpcSvcBuilderItf<SVC_T,CALL_T,BUILDER_T>
        > {

    private EmbeddedGenericServerFactory<SVC_T,CALL_T,BUILDER_T> _factory;

    runGenericEmbeddedServerTest(EmbeddedGenericServerFactory<SVC_T,CALL_T,BUILDER_T> factory)  {
        _factory = factory;
    }
    
    protected  EmbeddedGenericServer<SVC_T,CALL_T,BUILDER_T> createEmbeddedServer(int port) throws IOException {
        return _factory.createEmbeddedServer(port);
    }
    /**
     * What happens client-side when a call to proc 0 is done ?
     * Remark: libvirt does not define a proc 0 by default
     * @throws IOException
     */
    @Test(timeout=2000)
    public void testProc0() throws IOException {
        try ( EmbeddedGenericServer<SVC_T,CALL_T,BUILDER_T> srv = createEmbeddedServer (0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            RpcCallItf<SVC_T,CALL_T> caller = srv.getClientCall();
            int procNumber = srv.getAnyProc();
            System.out.println(procNumber);
            caller.call(0,XdrVoid.XDR_VOID,XdrVoid.XDR_VOID);
        }
    }
    /**
     * What happens client-side when a call to an undefined program is done
     * @throws IOException
     */
    @Test(timeout=2000,expected=OncRpcAcceptedException.class)
    public void testProcUnavailable() throws IOException {
        try (EmbeddedGenericServer<SVC_T,CALL_T,BUILDER_T>  srv = createEmbeddedServer (0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            RpcCallItf<SVC_T,CALL_T> caller = srv.getClientCall();
            assertNotNull(caller);
            int procNumber = srv.getUnavailableProc();
            System.out.println(procNumber);
            caller.call(procNumber,XdrVoid.XDR_VOID,XdrVoid.XDR_VOID);
        }
        fail("Unfinished test");
    }

    /**
     * What happens client-side when a call to an unknown version of an RPC program is done
     * @throws IOException
     */
    @Test(timeout=2000,expected=OncRpcAcceptedException.class)
    public void testVersionUnavailable() throws IOException {
        try (EmbeddedGenericServer<SVC_T,CALL_T,BUILDER_T>  srv = createEmbeddedServer (0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            RpcCallItf<SVC_T,CALL_T> caller = srv.getBadVersionClientCall();
            
            
            int procNumber = srv.getAnyProc();
            System.out.println(procNumber);
            caller.call(procNumber,XdrVoid.XDR_VOID,new XdrInt());
        }
        fail();
    }



    /**
     * What happens client-side when a call to an unknown RPC program is done
     * @throws IOException
     */
    @Test(timeout=2000,expected=OncRpcAcceptedException.class)
    public void testProgUnavailable() throws IOException {
        try (EmbeddedGenericServer<SVC_T,CALL_T,BUILDER_T>  srv = createEmbeddedServer (0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            RpcCallItf<SVC_T,CALL_T> caller = srv.getBadProgClientCall();
            
            
            int procNumber = srv.getAnyProc();
            System.out.println(procNumber);
            caller.call(procNumber,XdrVoid.XDR_VOID,new XdrInt());
        }
        fail();
    }

    /**
     * What happens client-side when a call to a known procedure fails server-side
     * @throws IOException
     */
    @Ignore
    @Test(timeout=2000,expected=OncRpcAcceptedException.class)
    public void testUnexpectedError() throws IOException {
        try ( EmbeddedGenericServer<SVC_T,CALL_T,BUILDER_T> srv = createEmbeddedServer (0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            RpcCallItf<SVC_T,CALL_T> caller = srv.getClientCall();
            int procNumber = srv.getAnyProc();
            System.out.println(procNumber);
            caller.call(srv.unexpectedErrorCall(),XdrVoid.XDR_VOID,new XdrInt());
        }
        fail();
    }
}
