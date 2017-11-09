package org.dcache.utils;

import static org.junit.Assert.*;

import java.io.IOException;

import org.dcache.xdr.XdrInt;
import org.dcache.xdr.XdrVoid;
import org.junit.Test;
import org.libvirt.VirRpcCall;

public class runEmbeddedServerTest {

    @Test(timeout=10000)
    public void testProcUnavailable() throws IOException {
        try ( EmbeddedVirtServer srv = new EmbeddedVirtServer(0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            VirRpcCall caller = srv.getClientCall();
            int procNumber = srv.getUnavailableProc();
            System.out.println(procNumber);
            caller.call(procNumber,XdrVoid.XDR_VOID,new XdrInt());
        }
        fail();
    }


    @Test(timeout=10000)
    public void testVersionUnavailable() throws IOException {
        try ( EmbeddedVirtServer srv = new EmbeddedVirtServer(0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            VirRpcCall caller = srv.getBadVersionClientCall();
            
            
            int procNumber = srv.getAnyProc();
            System.out.println(procNumber);
            caller.call(procNumber,XdrVoid.XDR_VOID,new XdrInt());
        }
        fail();
    }


    @Test(timeout=10000)
    public void testProgUnavailable() throws IOException {
        try ( EmbeddedVirtServer srv = new EmbeddedVirtServer(0)){
            assertTrue("Server should listen on a port != 0",srv.getListeningPort()>0);
            System.out.println(srv.getListeningPort());
            VirRpcCall caller = srv.getBadProgClientCall();
            
            
            int procNumber = srv.getAnyProc();
            System.out.println(procNumber);
            caller.call(procNumber,XdrVoid.XDR_VOID,new XdrInt());
        }
        fail();
    }
}
