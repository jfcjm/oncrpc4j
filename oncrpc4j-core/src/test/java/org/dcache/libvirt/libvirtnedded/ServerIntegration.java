package org.dcache.libvirt.libvirtnedded;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.RpcDispatchable;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.xdr.XdrInt;
import org.dcache.xdr.XdrString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.libvirt.VirOncRpcSvcBuilder;
import org.libvirt.VirRpcCall;

import static org.junit.Assert.*;

/**
 * The tool "virsh" have to be available on the test machine
 * 
 * THis test create a fake libvirt server able to answer to the virsh
 * "hostname" command.
 * 
 */
public class ServerIntegration {

    private static final int PROGNUM = 536903814;
    private static final int PROGVER = 1;

    private OncRpcSvc svc;
    private OncRpcSvc clnt;

    @Before
    public void setUp() throws IOException {
        List<Integer>  features = Arrays.asList(10,14,15);
        RpcDispatchable fakeLibvirtd = (RpcCall aCall) -> {
            assertTrue(aCall instanceof VirRpcCall);
            VirRpcCall call = (VirRpcCall) aCall;
            call.getXdr().asBuffer().mark();
            call.getXdr().asBuffer().reset();
            switch (call.getProcedure()) {

                case 1: {
                    XdrString s = new XdrString();
                    XdrAble connectArgs = new  XdrAble() {

                        String name;
                        int flags;
                        @Override
                        public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                            xdr.xdrDecodeInt();
                            name=xdr.xdrDecodeString();
                            flags = xdr.xdrDecodeInt();
                            assertEquals("test:///default",name);
                            assertEquals(0,flags);
                            
                        }

                        @Override
                        public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                            // TODO Auto-generated method stub
                            
                        }
                        
                    };
                    call.retrieveCall(connectArgs);
                    call.reply(s);
                    break;
                }
                case 2: {
                    svc.stop();
                    break;
                }
                case 66 : { 
                    //AUthlist : args void
                    XdrInt res = new XdrInt(0);
                    call.reply(res);
                    break;
                }
                case 60 : { //features : args int
                    
                    XdrInt res = new XdrInt();
                    call.retrieveCall(res);
                    assertTrue("unknown feature " + res.intValue(),features.contains(res.intValue()));
                    res = new XdrInt(0);
                    call.reply(res);
                    break;
                }
                case 59 : { 
                    XdrString res = new XdrString("fakelibvirthost");
                    call.reply(res);
                    break;
                }
                
                default :
                    fail("Unknown proc " + call.getProcedure());
            }
        };

        RpcDispatchable upper = (RpcCall call) -> {
            XdrString s = new XdrString();
            call.retrieveCall(s);
            XdrString u = new XdrString(s.stringValue().toUpperCase());
            call.reply(u);
        };

        svc = new VirOncRpcSvcBuilder()
                .withTCP()
                .withPort(20000)
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), fakeLibvirtd)
                .build();
        svc.start();
        
        /*
        clnt = new VirOncRpcSvcBuilder()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), upper)
                .build();
        clnt.start();
        XdrTransport t = clnt.connect(svc.getInetSocketAddress(IpProtocolType.TCP));
        */
    }

    @After
    public void tearDown() throws IOException {
        if (svc != null) {
            svc.stop();
        }
        if (clnt != null) {
            clnt.stop();
        }
    }
    @Test
    public void runServer()  throws IOException, InterruptedException{
        Thread.sleep(100000);
    }
    @Test
    public void callVirshAuthList() throws IOException{
        
        ProcessBuilder pb = new ProcessBuilder("virsh1", "--connect", "test+tcp://localhost:20000/default","hostname");
        Process process = pb.start();
        DataInputStream err = new DataInputStream(process.getErrorStream());
        String errResult = err.readLine();
        assertNull(errResult);
        DataInputStream out=new DataInputStream(process.getInputStream());
        assertEquals("fakelibvirthost",out.readLine());
        System.out.println();
    }
    

}
