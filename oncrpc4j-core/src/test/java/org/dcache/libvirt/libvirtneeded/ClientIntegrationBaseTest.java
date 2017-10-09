package org.dcache.libvirt.libvirtneeded;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrInt;
import org.dcache.xdr.XdrLong;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.XdrVoid;
import org.junit.Before;
import org.junit.Test;
import org.libvirt.VirOncRpcSvcBuilder;
import org.libvirt.VirRpcCall;
/**
 * 
 * Basic tests agains a running libvirtd.
 * 
 * These tests needs running libvirtd  on localhost listening 
 * on tcp/16509;
 * 
 * @author jmk
 *
 */
public class ClientIntegrationBaseTest {

    private static final String LIBVIRT_HOST    = "localhost";
    private static final int LIBVIRT_PORT       = 16509;
    
    private static final int PROGNUM            = 536903814;
    private static final int PROGVER            = 1;
    private VirRpcCall clntCall;
    
    @Before
    
    public void prepare() throws IOException {
        OncRpcSvc clnt = new VirOncRpcSvcBuilder()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .build();
        clnt.start();
        InetSocketAddress inetAddress = new InetSocketAddress(LIBVIRT_HOST, LIBVIRT_PORT);
        XdrTransport t = clnt.connect(inetAddress);
        clntCall = new VirRpcCall(PROGNUM, PROGVER, null, t);
        
        
    }
    @Test
    public void testGetFeature() throws IOException{
        XdrInt reply = new XdrInt();
        XdrInt askedFeature = new XdrInt(10);
        clntCall.call(60, askedFeature, reply);
        assertEquals(1,reply.intValue());
    }
    
    @Test(expected=IOException.class)
    public void testBadRPCNumber() throws IOException{
        XdrInt reply = new XdrInt();
        XdrInt askedFeature = new XdrInt(10);
        clntCall.call(1660, askedFeature, reply);
        assertEquals(1,reply.intValue());
    }
    
    @Test(expected=IOException.class)
    public void testBadArgs() throws IOException{
        XdrInt reply = new XdrInt();
        XdrAble askedFeature = XdrVoid.XDR_VOID;
        clntCall.call(1660, askedFeature, reply);
    }
    
    @Test//(expected=IOException.class)
    public void testBadReply() throws IOException{
        XdrAble reply = new XdrLong();
        XdrAble askedFeature = XdrVoid.XDR_VOID;
        clntCall.call(60, askedFeature, reply);
    }
}
