package org.dcache.utils;

import static org.junit.Assert.*;

import java.io.IOException;

import org.dcache.xdr.GenRpcSvc;
import org.dcache.xdr.OncRpcAcceptedException;
import org.dcache.xdr.GenRpcCall;
import org.dcache.xdr.XdrInt;
import org.dcache.xdr.XdrVoid;
import org.junit.Test;

public abstract class runGenericEmbeddedServerTest<
            SVC_T extends GenRpcSvc<SVC_T>,
            SRV_T extends EmbeddedGenericServer<SVC_T>> {



    protected abstract EmbeddedGenericServer<SVC_T> createEmbededServer(int port) throws IOException ;
    /**
     * What happens client-side when a call to proc 0 is done ?
     * Remark: libvirt does not define a proc 0 by default
     * @throws IOException
     */
    @Test(timeout=2000)
    public void testProc0() throws IOException {
        try ( EmbeddedGenericServer<SVC_T> srv = createEmbededServer (0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            GenRpcCall<SVC_T> caller = srv.getClientCall();
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
        try (EmbeddedGenericServer<SVC_T>  srv = createEmbededServer (0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            GenRpcCall<SVC_T> caller = srv.getClientCall();
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
        try (EmbeddedGenericServer<SVC_T>  srv = createEmbededServer (0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            GenRpcCall<SVC_T> caller = srv.getBadVersionClientCall();
            
            
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
        try (EmbeddedGenericServer<SVC_T>  srv = createEmbededServer (0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            GenRpcCall<SVC_T> caller = srv.getBadProgClientCall();
            
            
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
    @Test(timeout=2000,expected=OncRpcAcceptedException.class)
    public void testUnexpectedError() throws IOException {
        try ( EmbeddedGenericServer<SVC_T> srv = createEmbededServer (0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            GenRpcCall<SVC_T> caller = srv.getClientCall();
            int procNumber = srv.getAnyProc();
            System.out.println(procNumber);
            caller.call(srv.unexpectedErrorCall(),XdrVoid.XDR_VOID,new XdrInt());
        }
        fail();
    }
}
