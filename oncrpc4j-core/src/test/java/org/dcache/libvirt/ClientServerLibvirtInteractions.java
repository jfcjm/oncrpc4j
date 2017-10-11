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

import java.io.DataInputStream;
import java.io.IOException;

import org.dcache.xdr.IpProtocolType;
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
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.XdrVoid;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.libvirt.VirOncRpcSvcBuilder;
import org.libvirt.VirRpcCall;

import static org.junit.Assert.*;

/**
 * Interop tests between a virtrpc client and a server 
 * 
 */
public class ClientServerLibvirtInteractions {

    private static final int PROGNUM = 536903814;
    private static final int PROGVER = 1;

    private OncRpcSvc svc;
    private OncRpcSvc clnt;
    private VirRpcCall clntCall;

    @Before
    public void setUp() throws IOException {

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
                            xdr.xdrDecodeBoolean(); 
                            name=xdr.xdrDecodeString();
                            flags = xdr.xdrDecodeInt();
                            assertEquals("test:///default",name);
                            assertEquals(0,flags);
                            
                        }

                        @Override
                        public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
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
                    XdrInt res = new XdrInt(0);
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

        svc = new VirOncRpcSvcBuilder()
                .withTCP()
                .withPort(20000)
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), fakeLibvirtd)
                .build();
        svc.start();
        
       
        clnt = new VirOncRpcSvcBuilder()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .build();
        clnt.start();
        XdrTransport t = clnt.connect(svc.getInetSocketAddress(IpProtocolType.TCP));
        clntCall = new VirRpcCall(PROGNUM, PROGVER, null, t);
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
    public void callVirshAuthList() throws IOException{
        XdrInt reply = new XdrInt();
        clntCall.call(66, XdrVoid.XDR_VOID, reply);
        assertEquals(0,reply.intValue());
        XdrAble connectArgs = new  XdrAble() {
            @Override
            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                
            }

            @Override
            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeBoolean(true);
                xdr.xdrEncodeString("test:///default");
                xdr.xdrEncodeInt(0);
                
            }
            
        };
        clntCall.call(1,connectArgs ,  XdrVoid.XDR_VOID);
    }
    

}
