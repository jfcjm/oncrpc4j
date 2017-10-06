package org.dcache.libvirt;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.XdrInt;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.XdrVoid;
import org.junit.Before;
import org.junit.Test;
import org.libvirt.VirOncRpcSvcBuilder;
import org.libvirt.VirRpcCall;
/**
 * These tests needs a running libvirtd running on localhost.
 * @author jmk
 *
 */
public class ClientIntegrationTest {


    private static final int PROGNUM = 536903814;
    private static final int PROGVER = 1;
    private VirRpcCall clntCall;
    
    @Before
    
    public void prepare() throws IOException {
        OncRpcSvc clnt = new VirOncRpcSvcBuilder()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .build();
        clnt.start();
        InetSocketAddress inetAddress = new InetSocketAddress("localhost", 16509);
        XdrTransport t = clnt.connect(inetAddress);
        clntCall = new VirRpcCall(PROGNUM, PROGVER, null, t);
        
        
    }
    @Test
    public void testGetAuthLList() throws IOException{
        XdrInt reply = new XdrInt();
        clntCall.call(66, XdrVoid.XDR_VOID, reply);
        assertEquals(0,reply.intValue());
    }
}
