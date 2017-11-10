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

package org.dcache.libvirt;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslServer;

import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.xdr.XdrInt;
import org.dcache.xdr.XdrString;
import org.dcache.xdr.XdrVoid;
import org.dcache.xdr.model.itf.GenItfRpcSvc;
import org.dcache.xdr.model.itf.GenItfXdrTransport;
import org.dcache.xdr.model.itf.GenRpcDispatchable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.libvirt.GenVirOncRpcSvcBuilder;
import org.libvirt.GenVirOncRpcSvc;
import org.libvirt.GenVirRpcCall;
import org.libvirt.SASLPacketWrapper;

import static org.junit.Assert.*;

/**
 * This testsuite emulate a simple libvirtd process, the tool "virsh" 
 * have to be available
 * 
 * THis test create a fake libvirt server able to answer to the virsh
 * "hostname" command.
 * 
 */
public class ServerIntegrationWithSASL {

    private static final int PROGNUM = 536903814;
    private static final int PROGVER = 1;
    private GenItfRpcSvc svc;
    private GenItfRpcSvc clnt;

    
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
                           AuthorizeCallback ac = (AuthorizeCallback) cb;
                           ((AuthorizeCallback) cb).setAuthorized(true);
                       }
                      }
                 }
            
            
        };
        props.put(Sasl.QOP,"auth-conf");
        SaslServer saslSrv = Sasl.createSaslServer("DIGEST-MD5", "libvirt", "localhost", props, cbh);
        
        GenRpcDispatchable fakeLibvirtd = ( aCall) -> {
            assertTrue(aCall instanceof GenVirRpcCall);
            GenVirRpcCall call =  (GenVirRpcCall) aCall;
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
                    //svc.stop();
                    call.reply(XdrVoid.XDR_VOID);
                    break;
                }
                case 60 : { //features : args int
                    XdrInt res = new XdrInt(0);
                    call.reply(res);
                    break;
                }
                case 59 : { 
                    XdrString res = new XdrString("fakelibvirthost");
                    call.reply(res);
                    break;
                }
                case 66 : { 
                    //AUthlist : args void
                    
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
                    XdrString res = new XdrString("DIGEST-MD5");
                    call.reply(res);
                    break;
                }
                case 68 : { 
                    
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
                    
                    byte[] challenge = saslSrv.evaluateResponse(args.data);
                    //assertEquals(148,challenge.length);
                    XdrAble res = new XdrAble(){

                        @Override
                        public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                            // TODO Auto-generated method stub
                            
                        }

                        @Override
                        public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                            assertFalse(saslSrv.isComplete());
                            xdr.xdrEncodeInt(saslSrv.isComplete() ?1 :0);
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
                    byte[] challenge = saslSrv.evaluateResponse(args.data);

                    assertTrue(saslSrv.isComplete());
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
                    ((GenVirOncRpcSvc) svc).setPacketWrapperAfterNextWrite(new SASLPacketWrapper(saslSrv));
                    call.reply(res);
                    break;
                }
                default :
                    fail("Unknown proc " + call.getProcedure());
            }
        };

        GenRpcDispatchable upper = ( call) -> {
            XdrString s = new XdrString();
            call.retrieveCall(s);
            XdrString u = new XdrString(s.stringValue().toUpperCase());
            call.reply(u);
        };

        svc = new GenVirOncRpcSvcBuilder()
                .withTCP()
                .withPort(20000)
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), fakeLibvirtd)
                .build();
        svc.start();
        
        
        clnt = new GenVirOncRpcSvcBuilder()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), upper)
                .build();
        clnt.start();
          GenItfXdrTransport t = clnt.connect(svc.getInetSocketAddress(IpProtocolType.TCP));
        
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
    public void callVirshAuthList() throws IOException, InterruptedException{
        
        ProcessBuilder pb = new ProcessBuilder("virsh","--connect", "test+tcp://localhost:20000/default?authfile=src/test/resources/libvirtauth.conf","hostname");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        StringBuilder sb = new StringBuilder();
        String result = null;
        
        DataOutputStream os = new DataOutputStream(process.getOutputStream());
        

        
        
        
        BufferedReader out=new BufferedReader(new InputStreamReader(process.getInputStream()));
        System.out.println("wait for output");
        char [] cbuf = new  char ["fakelibvirthost".length()];
        int intResult = out.read(cbuf);
        assertEquals("fakelibvirthost",new String(cbuf));
    }
    

}
