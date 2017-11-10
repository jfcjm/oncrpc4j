package org.dcache.utils;
import static org.junit.Assert.*;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.Map;

import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.model.itf.GenItfXdrTransport;
import org.libvirt.GenVirOncRpcSvc;
import org.libvirt.GenVirOncRpcSvcBuilder;
import org.libvirt.GenVirRpcCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedVirtServer implements Closeable{
    private final static Logger _log = LoggerFactory.getLogger(EmbeddedVirtServer.class);
    
    private static final int PROGNUM = 536903814;
    private static final int PROGVER = 1;
    private static final int LIBVIRT_PORT = 16509;
    
    Map<Integer,GenVirRpcDispatchable> srcActions = new HashMap<>();

    GenVirRpcDispatchable fakeLibvirtd = ( call) ->
    {
        GenVirRpcDispatchable action = srcActions.get(call.getProcedure());
        if (null != action){
            action.dispatchOncRpcCall(call);
        } else {
            _log.warn("Procedure #{} is not defined");
            call.failProcedureUnavailable();
        }
    };


    private GenVirOncRpcSvc client;
    
    private GenVirOncRpcSvc svc;

   

    
    public EmbeddedVirtServer() throws IOException{
        this(LIBVIRT_PORT);
    }
    
    public EmbeddedVirtServer(boolean hasDynamicport) throws IOException{
        this(LIBVIRT_PORT);
    }
    public EmbeddedVirtServer(int port) throws IOException {
        // Le typ retoruné dépend du type de l'argument fakeLibvirtd
        svc = new GenVirOncRpcSvcBuilder()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), fakeLibvirtd)
                .withPort(port)
                .withWorkerThreadIoStrategy()
                .build();
        assertTrue(svc instanceof GenVirOncRpcSvc);
        svc.start();
    }


    private InetSocketAddress getAddress() {
        return svc.getInetSocketAddress(IpProtocolType.TCP);
    }
    public int getListeningPort() {
        return svc.getInetSocketAddress(IpProtocolType.TCP).getPort();
    }
    
    public GenItfXdrTransport getTransport() throws IOException {
        return svc.connect(getAddress());
        
    }

    public GenVirRpcCall getClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM,PROGVER);
    }

    private GenVirRpcCall createClient(int prognum, int progver) throws IOException {
        client = new GenVirOncRpcSvcBuilder()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .build();
        client.start();
         GenItfXdrTransport<GenVirOncRpcSvc> t = client.connect(svc.getInetSocketAddress(IpProtocolType.TCP));
        return new GenVirRpcCall(prognum, progver, null, t);
    }
    
    public void close() throws IOException{
        close(client);
        close(svc);
    }

    private void close(GenVirOncRpcSvc svc) throws IOException {
        if (svc != null) svc.stop();
    }

    public int getUnavailableProc() {
        int randomNum = (int)Math.random();
        while (isAvailbale(randomNum)){
            randomNum = (int)Math.random();
        }
        return randomNum;
    }

    private boolean isAvailbale(int randomNum) {
        return null != srcActions.get(randomNum);
    }

    public GenVirRpcCall getBadProgClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM+12,PROGVER);
    }

    public int getAnyProc() {
        return 0;
    }

    public GenVirRpcCall getBadVersionClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM,PROGVER+12);
    }
    
}
