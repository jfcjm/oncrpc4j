package org.dcache.xdr;

import java.io.EOFException;
import java.io.IOException;

import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.itf.RpcDispatchable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.dcache.utils.ConversionUtils.nullCompletionHandler;
import static org.junit.Assert.*;

/**
 *
 */
public class GenClientServerTest2 {

    private static final int PROGNUM = 100017;
    private static final int PROGVER = 1;

    private static final int ECHO = 1;
    private static final int UPPER = 2;
    private static final int SHUTDOWN = 3;

    private OncRpcSvc svc;
    private OncRpcSvc clnt;
    private IRpcCall clntCall;

    @Before
    public void setUp() throws IOException {

        RpcDispatchable<OncRpcSvc> echo = ( call) -> {
            switch (call.getProcedure()) {

                case ECHO: {
                    XdrString s = new XdrString();
                    call.retrieveCall(s);
                    call.reply(s);
                    break;
                }
                case UPPER: {
                    IRpcCall cb =  IRpcCall.getImpl(PROGNUM, PROGVER, new RpcAuthTypeNone(), call.getTransport());
                    XdrString s = new XdrString();
                    call.retrieveCall(s);
                    cb.call(ECHO, s, s);
                    call.reply(s);
                    break;
                }
                case SHUTDOWN: {
                    svc.stop();
                }
            }
        };

        RpcDispatchable<OncRpcSvc> upper = ( call) -> {
            XdrString s = new XdrString();
            call.retrieveCall(s);
            XdrString u = new XdrString(s.stringValue().toUpperCase());
            call.reply(u);
        };

        svc = IOncRpcSvcBuilder.getImpl()
                .withoutAutoPublish()
                .withTCP()
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), echo)
                .build();
        svc.start();

        clnt =  IOncRpcSvcBuilder.getImpl()
                .withoutAutoPublish()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), upper)
                .build();
        clnt.start();
        XdrTransportItf<OncRpcSvc> t = clnt.connect(svc.getInetSocketAddress(IpProtocolType.TCP));
        clntCall =  IRpcCall.getImpl(PROGNUM, PROGVER, new RpcAuthTypeNone(), t);
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

        clntCall.call(ECHO, s, nullCompletionHandler());
    }

    @Test(expected = EOFException.class, timeout = 5000)
    public void shouldFailClientCallWhileWaitingWhenServerStopped() throws IOException, InterruptedException {
        XdrString s = new XdrString("hello");

        clntCall.call(SHUTDOWN, s, s);
    }

    @Test
    public void shouldTriggerClientCallbackEvenIfOtherClientDisconnected() throws IOException {

        OncRpcSvc clnt2 =  IOncRpcSvcBuilder.getImpl()
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
