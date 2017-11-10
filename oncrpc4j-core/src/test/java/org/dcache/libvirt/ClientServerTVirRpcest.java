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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.CompletionHandler;

import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.XdrString;
import org.dcache.xdr.XdrVoid;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.itf.GenRpcDispatchable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.libvirt.GenVirOncRpcSvc;
import org.libvirt.GenVirOncRpcSvcBuilder;
import org.libvirt.GenVirRpcCall;

/**
 * These tests use both a virrpc serer and client. It is adapted
 * oncprcServerTest
 */
public class ClientServerTVirRpcest {

    private static final int PROGNUM = 100017;
    private static final int PROGVER = 1;

    private static final int ECHO = 1;
    private static final int UPPER = 2;
    private static final int SHUTDOWN = 3;

    private GenVirOncRpcSvc svc;
    private RpcSvcItf clnt;
    private GenVirRpcCall clntCall;

    @Before
    public void setUp() throws IOException {

        GenRpcDispatchable<GenVirOncRpcSvc> echo = ( aCall) -> {
            assertTrue(aCall instanceof GenVirRpcCall);
            GenVirRpcCall call = (GenVirRpcCall) aCall;
            call.getXdr().asBuffer().mark();
            assertEquals(0,call.getXdr().asBuffer().get());
            assertEquals(0,call.getXdr().asBuffer().get());
            assertEquals(0,call.getXdr().asBuffer().get());
            assertEquals(5,call.getXdr().asBuffer().get());
            call.getXdr().asBuffer().reset();
            switch (call.getProcedure()) {

                case ECHO: {
                    XdrString s = new XdrString();
                    call.retrieveCall(s);
                    assertEquals("hello",s.stringValue());
                    call.reply(s);
                    break;
                }
                case UPPER: {
                    GenVirRpcCall cb = new GenVirRpcCall(PROGNUM, PROGVER, null, call.getTransport());
                    XdrString s = new XdrString();
                    call.retrieveCall(s);
                    assertEquals("hello",s.stringValue());
                    cb.call(ECHO, s, s);
                    call.reply(s);
                    break;
                }
                case SHUTDOWN: {
                    svc.stop();
                    break;
                }
                default :
                    fail("Unknown proc");
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
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), echo)
                .build();
        svc.start();

        clnt = new GenVirOncRpcSvcBuilder()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), upper)
                .build();
        clnt.start();
        XdrTransportItf<GenVirOncRpcSvc> t = clnt.connect(svc.getInetSocketAddress(IpProtocolType.TCP));
        clntCall = new GenVirRpcCall(PROGNUM, PROGVER, null, t);
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
    public void shouldCallCorrectProcedure() throws IOException {
        XdrString s = new XdrString("hello");
        XdrString reply = new XdrString();
        clntCall.call(ECHO, s, reply);

        assertEquals("reply mismatch", s, reply);
    }

    @Test
    public void shouldTriggerClientCallback() throws IOException {
        XdrString s = new XdrString("hello");
        XdrString reply = new XdrString();

        clntCall.call(UPPER, s, reply);

        assertEquals("reply mismatch", s.stringValue().toUpperCase(), reply.stringValue());
    }

    @Test(expected = EOFException.class, timeout = 5000)
    public void shouldFailClientCallWhenServerStopped() throws IOException, InterruptedException {
        XdrString s = new XdrString("hello");

        try {
            // stop the server
            clntCall.call(SHUTDOWN, s, XdrVoid.XDR_VOID);
        } catch (EOFException e) {
            // ignore disconnect error
        }

        clntCall.call(ECHO, s, (CompletionHandler) null);
    }

    @Test(expected = EOFException.class, timeout = 5000)
    public void shouldFailClientCallWhileWaitingWhenServerStopped() throws IOException, InterruptedException {
        XdrString s = new XdrString("hello");

        clntCall.call(SHUTDOWN, s, s);
    }

    @Test
    public void shouldTriggerClientCallbackEvenIfOtherClientDisconnected() throws IOException {

        GenVirOncRpcSvc clnt2 = new GenVirOncRpcSvcBuilder()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .build();
        clnt2.start();
        clnt2.connect(svc.getInetSocketAddress(IpProtocolType.TCP));
        clnt2.stop();

        XdrString s = new XdrString("hello");
        XdrString reply = new XdrString();

        clntCall.call(UPPER, s, reply);

        assertEquals("reply mismatch", s.stringValue().toUpperCase(), reply.stringValue());
    }

}
