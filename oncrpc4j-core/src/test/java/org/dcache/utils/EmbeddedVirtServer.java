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
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.RpcDispatchable;
import org.dcache.xdr.XdrTransport;
import org.libvirt.VirOncRpcSvc;
import org.libvirt.VirOncRpcSvcBuilder;
import org.libvirt.VirRpcCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedVirtServer implements Closeable{
    private final static Logger _log = LoggerFactory.getLogger(EmbeddedVirtServer.class);
    
    private static final int PROGNUM = 536903814;
    private static final int PROGVER = 1;
    private static final int LIBVIRT_PORT = 16509;
    
    Map<Integer,RpcDispatchable> srcActions = new HashMap<Integer,RpcDispatchable>();

    RpcDispatchable fakeLibvirtd = (RpcCall call) ->
    {
        RpcDispatchable action = srcActions.get(call.getProcedure());
        if (null != action){
            action.dispatchOncRpcCall(call);
        } else {
            _log.warn("Procedure #{} is not defined");
            call.failProcedureUnavailable();
        }
    };

    private OncRpcSvc svc;

    private OncRpcSvc client;
    
    public EmbeddedVirtServer() throws IOException{
        this(LIBVIRT_PORT);
    }
    
    public EmbeddedVirtServer(boolean hasDynamicport) throws IOException{
        this(LIBVIRT_PORT);
    }
    public EmbeddedVirtServer(int port) throws IOException {
        svc = new VirOncRpcSvcBuilder()
                .withTCP()
                .withPort(port)
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), fakeLibvirtd)
                .build();
        assertTrue(svc instanceof VirOncRpcSvc);
        svc.start();
    }


    private InetSocketAddress getAddress() {
        return svc.getInetSocketAddress(IpProtocolType.TCP);
    }
    public int getListeningPort() {
        return svc.getInetSocketAddress(IpProtocolType.TCP).getPort();
    }
    
    public XdrTransport getTransport() throws IOException {
        return svc.connect(getAddress());
        
    }

    public VirRpcCall getClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM,PROGVER);
    }

    private VirRpcCall createClient(int prognum, int progver) throws IOException {
        client = new VirOncRpcSvcBuilder()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .build();
        client.start();
        XdrTransport t = client.connect(svc.getInetSocketAddress(IpProtocolType.TCP));
        return new VirRpcCall(prognum, progver, null, t);
    }
    
    public void close() throws IOException{
        close(client);
        close(svc);
    }

    private void close(OncRpcSvc svc) throws IOException {
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

    public VirRpcCall getBadProgClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM+12,PROGVER);
    }

    public int getAnyProc() {
        return 0;
    }

    public VirRpcCall getBadVersionClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM,PROGVER+12);
    }
    
}
