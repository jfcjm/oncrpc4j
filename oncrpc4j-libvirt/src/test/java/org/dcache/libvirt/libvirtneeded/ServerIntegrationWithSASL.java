/*******************************************************************************
 * Copyright (C) 2017 INU Champollion, Albi, France
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 ******************************************************************************/

package org.dcache.libvirt.libvirtneeded;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

import org.dcache.libvirt.VirRpcDispatchable;
import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.xdr.XdrInt;
import org.dcache.xdr.XdrString;
import org.dcache.xdr.XdrLong;
import org.dcache.xdr.XdrVoid;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.junit.After;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.libvirt.VirRpcSvcBuilder;
import org.libvirt.VirRpcSvc;
import org.libvirt.VirRpcCall;
import org.libvirt.SASLPacketWrapper;

import static org.junit.Assert.*;

/**
 * This testsuite emulate a simple libvirtd process, the tool "virsh" 
 * have to be available
 * 
 * THis test create a fake libvirt server able to answer to the virsh
 * "hostname" command. This server will not be able to serve more than 
 * one client by invocation (no session management).
 * 
 */
public class ServerIntegrationWithSASL {
    
    public class CallCtx {
        private CallCtx() {};
        SaslServer _saslSrv;
        private Map<String, ?> _props;
        private CallbackHandler _cbh;
        public CallCtx(Map<String, String> props, CallbackHandler cbh) throws SaslException {
            this();
            _props = props;
            _cbh = cbh;
        }
        public void activate () throws SaslException {
            _saslSrv = Sasl.createSaslServer("DIGEST-MD5", "libvirt", "localhost", _props, _cbh);
        }
        public void dispose() throws SaslException {
            _saslSrv.dispose();  
        }
        public byte[] evaluateResponse(byte[] data) throws SaslException {
            return  _saslSrv.evaluateResponse(data);
        }
        public boolean isComplete() {
            return _saslSrv.isComplete();
        }
        public SaslServer getSaslSrv() {
            return _saslSrv;
        }
    }

    private static final int PROGNUM = 536903814;
    private static final int PROGVER = 1;
    private VirRpcSvc svc;
    private VirRpcSvc clnt;

    
    @Before
    public void setUp() throws IOException {

        Map<String, String> props = new HashMap<>();
        
        
        
        CallbackHandler cbh = new CallbackHandler(){

            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback cb : callbacks) {
                    System.out.println(cb.getClass().getName());
                    if (cb instanceof NameCallback) {

                        System.out.println("Client - NameCallback");

                        NameCallback nc = (NameCallback)cb;
                        nc.setName("fakeuser");
                       } else if (cb instanceof PasswordCallback) {

                        System.out.println("Client - PasswordCallback");

                        PasswordCallback pc = (PasswordCallback)cb;
                        pc.setPassword("fakepassword".toCharArray());
                        
                       } else if (cb instanceof AuthorizeCallback) {
                           @SuppressWarnings("unused")
                        AuthorizeCallback ac = (AuthorizeCallback) cb;
                           ((AuthorizeCallback) cb).setAuthorized(true);
                       }
                      }
                 }
            
            
        };
        props.put(Sasl.QOP,"auth-conf");
        

        CallCtx saslCtx = new CallCtx(props, cbh);
        //The type GenVirRpcDispatchable is used at is implies the type infernce
        //for builder withRpcService
        VirRpcDispatchable fakeLibvirtd = ( aCall) -> {
            assertTrue(aCall instanceof VirRpcCall);
            VirRpcCall call =  (VirRpcCall) aCall;
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
                            xdr.xdrDecodeBoolean();
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
                    //close
                    call.retrieveCall(XdrVoid.XDR_VOID);
                    svc.unsetPacketWrapper();
                    call.reply(XdrVoid.XDR_VOID);
                    saslCtx.dispose();
                    break;
                }
                case 3: {
                    //type 
                    call.retrieveCall(XdrVoid.XDR_VOID);
                    call.reply(new XdrString("reer"));
                    break;
                }
                case 4: {
                    //version
                    
                    call.retrieveCall(XdrVoid.XDR_VOID);
                    call.reply(new XdrLong(12L));
                    break;
                }
                case 7: {
                    //capabilities
                    String capabilities  = new Scanner(new File("src/test/resources/caps-test2.xml")).useDelimiter("\\Z").next();
                    assertNotNull(capabilities);
                    call.retrieveCall(XdrVoid.XDR_VOID);
                    
                    
                    call.reply(new XdrString(capabilities));
                    break;
                }
                case 60 : { //features : args int
                    call.retrieveCall(XdrVoid.XDR_VOID);
                    XdrInt res = new XdrInt(0);
                    call.reply(res);
                    break;
                }
                case 59 : { 
                    call.retrieveCall(XdrVoid.XDR_VOID);
                    XdrString res = new XdrString("fakelibvirthost");
                    call.reply(res);
                    break;
                }
                case 66 : { 
                    //AUthlist : args void
                    call.retrieveCall(XdrVoid.XDR_VOID);
                    XdrAble res = new XdrAble(){

                        @Override
                        public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                            // TODO Auto-generated method stub
                            
                        }

                        @Override
                        public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                            int [] result = {1};
                            xdr.xdrEncodeIntVector(result);
                            
                        }
                        
                    };
                    call.reply(res);
                    break;
                }
                case 67 : { 
                    //SASL INIT
                    call.retrieveCall(XdrVoid.XDR_VOID);
                    XdrString res = new XdrString("DIGEST-MD5");
                    call.reply(res);
                    break;
                }
                case 68 : { 
                    //START
                    class SASLStartArgs implements XdrAble{
                        public byte[] data;
                        @Override
                        public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                            assertEquals("DIGEST-MD5",xdr.xdrDecodeString());
                            xdr.xdrDecodeBoolean ();
                            assertEquals(0,xdr.xdrDecodeInt());
                            data = xdr.xdrDecodeByteVector();
                            assertEquals(0,data.length);
                        }

                        @Override
                        public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                        }
                    };
                    SASLStartArgs args = new SASLStartArgs();
                    call.retrieveCall(args);
                    saslCtx.activate();
                    byte[] challenge = saslCtx.evaluateResponse(args.data);
                    System.out.println("Challenge" + new String(challenge));
                    //assertEquals(148,challenge.length);
                    XdrAble res = new XdrAble(){

                        @Override
                        public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                            // TODO Auto-generated method stub
                            
                        }

                        @Override
                        public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                            assertFalse(saslCtx.isComplete());
                            xdr.xdrEncodeInt(saslCtx.isComplete() ?1 :0);
                            xdr.xdrEncodeInt(0);//nil ??
                            xdr.xdrEncodeByteVector(challenge);
                        }
                        
                    };
                    call.reply(res);
                    break;
                }
                case 69 : { //sasl step
                    class SASLStepArgs implements XdrAble{
                        public byte[] data;
                        @Override
                        public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                            xdr.xdrDecodeInt(); // nil
                            data = xdr.xdrDecodeByteVector();
                        }
                        
                        @Override
                        public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                        }
                    };
                    SASLStepArgs args = new SASLStepArgs();
                    call.retrieveCall(args);
                    byte[] challenge = saslCtx.evaluateResponse(args.data);

                    assertTrue(saslCtx.isComplete());
                    XdrAble res = new XdrAble(){

                        @Override
                        public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                            // TODO Auto-generated method stub
                            
                        }

                        @Override
                        public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                            xdr.xdrEncodeInt(1);
                            xdr.xdrEncodeInt(0);//nil ??
                            xdr.xdrEncodeByteVector(challenge);
                        }
                        
                    };
                    svc.setPacketWrapperAfterNextWrite(new SASLPacketWrapper(saslCtx.getSaslSrv()));
                    call.reply(res);
                    break;
                }
                default :
                    fail("Unknown proc " + call.getProcedure());
            }
        };
        
       VirRpcDispatchable upper = ( call) -> {
            XdrString s = new XdrString();
            call.retrieveCall(s);
            XdrString u = new XdrString(s.stringValue().toUpperCase());
            call.reply(u);
        };

        svc =  VirRpcSvcBuilder.getImpl()
        
                .withTCP()
                .withPort(20000)
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), fakeLibvirtd)
                .build()
                ;
        svc.start();
        
        
        clnt =  VirRpcSvcBuilder.getImpl()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), upper)
                .build();
        clnt.start();
          @SuppressWarnings("unused")
        XdrTransportItf<VirRpcSvc> t = clnt.connect(svc.getInetSocketAddress(IpProtocolType.TCP));
        
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
    @Test(timeout=3000)
    public void callVirshHostname() throws IOException, InterruptedException{
        BufferedReader out = getVirshOutput("hostname");
        try {
        assertEquals("fakelibvirthost",out.readLine());
        } catch (ComparisonFailure e){
            System.out.println(out.readLine());
            throw e;
        }
    }
    
    
    @Test(timeout=3000)
    public void callVirshVersion() throws IOException, InterruptedException{
        BufferedReader out = getVirshOutput("version");
        
        assertEquals("Compiled against library: libvirt 3.0.0",out.readLine());
        assertEquals("Using library: libvirt 3.0.0",out.readLine());
        assertEquals("Using API: reer 3.0.0",out.readLine());
        assertEquals("Running hypervisor: reer 0.0.12",out.readLine());
        
       
       
        
    }
    @Test(timeout=3000)
    public void callVirshIfaceDumpXml() throws IOException, InterruptedException{
        
        BufferedReader out = getVirshOutput("capabilities");
        try (Scanner s =new Scanner(out) ) {
            assertEquals(3384,s.useDelimiter("\\Z").next().length());
        }
        
    }
    
    @Test(timeout=3000)
    public void callVirshtwocalls() throws IOException, InterruptedException{
        
        BufferedReader out = getVirshOutput("hostname");
        assertEquals("fakelibvirthost",out.readLine());out = getVirshOutput("version");
        assertEquals("Compiled against library: libvirt 3.0.0",out.readLine());
        assertEquals("Using library: libvirt 3.0.0",out.readLine());
        assertEquals("Using API: reer 3.0.0",out.readLine());
        assertEquals("Running hypervisor: reer 0.0.12",out.readLine());
    }
    
    private BufferedReader getVirshOutput(String command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("virsh","--connect", "test+tcp://localhost:20000/default?authfile=src/test/resources/libvirtauth.conf",command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        BufferedReader out=new BufferedReader(new InputStreamReader(process.getInputStream()));
        return out;
    }
    

}
