package org.dcache.libvirt.libvirtnedded;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.xdr.XdrInt;
import org.dcache.xdr.XdrString;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.XdrVoid;
import org.junit.Before;
import org.junit.Test;
import org.libvirt.VirOncRpcSvcBuilder;
import org.libvirt.VirRpcCall;

import tests.credentials.TestCredentials4Libvirt;

/**
 * This tests needs a running libvirtd running on localhost. 
 * 
 * It mimics virsh exchange sequence against a libvirtd running 
 * on localhost : 
 * 
 * authlist?void
 *      authlist!int[]{0} 
 * featureSupported?10 
 *      featureSupported!1
 * connectOpen?true,"test:///default"
 *      connectOpen!void 
 * featureSupported?14
 *      featureSupported!1 
 * featureSupported?15 
 *      featureSupported!1
 * connectHostname?void 
 *      connectHostname!LIBVIRTHOSTNAME
 * connectClose?void 
 *      connectClose!void 
 *      
 * @author jmk
 *
 */
public class ClientIntegrationSequenceNoAuthTest {

    private static final String LIBVIRTD_HOSTNAME   = TestCredentials4Libvirt.LIBVIRTD_INTEGRATION_HOSTNAME;
    private static final String TARGET_HOST         = TestCredentials4Libvirt.TARHET_HOST;
    private static final int LIBVIRT_PORT = 16509;
    
    
    private static final int PROGNUM = 536903814;
    private static final int PROGVER = 1;
    private VirRpcCall clntCall;
    private OncRpcSvc clnt;
    private XdrTransport t;

    @Before

    public void prepare() throws IOException {
        clnt = new VirOncRpcSvcBuilder().withTCP().withClientMode().withWorkerThreadIoStrategy().build();
        clnt.start();
        InetSocketAddress inetAddress = new InetSocketAddress(TARGET_HOST, 16509);
        t = clnt.connect(inetAddress);
        clntCall = new VirRpcCall(PROGNUM, PROGVER, null, t);
    }

    @Test
    public void testGetAuthLList() throws IOException {
        
        {
            XdrAble reply = new XdrAble() {

                @Override
                public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                    int[] res = xdr.xdrDecodeIntVector();
                    assertEquals("Length of auth method array shouls be 1",1, res.length);
                    assertEquals("Length of auth method array shouls be 0 (NONE)", 0, res[0]);
                }

                @Override
                public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                    // TODO Auto-generated method stub

                }

            };
            clntCall.call(66, XdrVoid.XDR_VOID, reply);
        }
        {
            XdrInt reply = new XdrInt();
            XdrInt askedFeature = new XdrInt(10);
            clntCall.call(60, askedFeature, reply);
            assertEquals(1, reply.intValue());
        }
        {
            XdrAble connectArg = new XdrAble() {

                @Override
                public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {

                }

                @Override
                public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                    { //libvirt remote strings are encoded
                      // with a boolean and then a nullable string
                        xdr.xdrEncodeBoolean(true);
                        xdr.xdrEncodeString("test:///default");
                    }

                    xdr.xdrEncodeInt(0);
                }

            };
            clntCall.call(1, connectArg, XdrVoid.XDR_VOID);
        }

        {
            XdrInt reply = new XdrInt();
            XdrInt askedFeature = new XdrInt(14);
            clntCall.call(60, askedFeature, reply);
            assertEquals(1, reply.intValue());
        }

        {
            XdrInt reply = new XdrInt();
            XdrInt askedFeature = new XdrInt(15);
            clntCall.call(60, askedFeature, reply);
            assertEquals(1, reply.intValue());
        }

        {
            XdrString reply = new XdrString();
            clntCall.call(59, XdrVoid.XDR_VOID, reply);
            assertEquals(LIBVIRTD_HOSTNAME, reply.toString());
        }

        {
            clntCall.call(2, XdrVoid.XDR_VOID, XdrVoid.XDR_VOID);
        }
        {
        }
    }
}
