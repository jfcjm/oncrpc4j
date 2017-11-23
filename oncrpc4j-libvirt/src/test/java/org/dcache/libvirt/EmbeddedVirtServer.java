package org.dcache.libvirt;
import static org.junit.Assert.*;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.Map;

import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.model.itf.RpcDispatchable;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.libvirt.VirRpcCall;
import org.libvirt.VirRpcSvc;
import org.libvirt.VirRpcSvcBuilder;
import org.libvirt.IVirRpcCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedVirtServer implements Closeable{
    private final static Logger _log = LoggerFactory.getLogger(EmbeddedVirtServer.class);
    
    private static final int PROGNUM = 536903814;
    private static final int PROGVER = 1;
    private static final int LIBVIRT_PORT = 16509;
    
    Map<Integer,VirRpcDispatchable> srcActions = new HashMap<>();

    RpcDispatchable<VirRpcSvc> fakeLibvirtd = ( call) ->
    {
        VirRpcDispatchable action = srcActions.get(call.getProcedure());
        if (null != action){
            action.dispatchOncRpcCall(call);
        } else {
            _log.warn("Procedure #{} is not defined");
            call.failProcedureUnavailable();
        }
    };


    private VirRpcSvc client;
    
    private VirRpcSvc svc;

   

    
    public EmbeddedVirtServer() throws IOException{
        this(LIBVIRT_PORT);
    }
    
    public EmbeddedVirtServer(boolean hasDynamicport) throws IOException{
        this(LIBVIRT_PORT);
    }
    public EmbeddedVirtServer(int port) throws IOException {
        // Le typ retoruné dépend du type de l'argument fakeLibvirtd
        svc =  VirRpcSvcBuilder.getImpl()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), fakeLibvirtd)
                .withPort(port)
                .withWorkerThreadIoStrategy()
                .build();
        assertTrue(svc instanceof VirRpcSvc);
        svc.start();
    }


    private InetSocketAddress getAddress() {
        return svc.getInetSocketAddress(IpProtocolType.TCP);
    }
    public int getListeningPort() {
        return svc.getInetSocketAddress(IpProtocolType.TCP).getPort();
    }
    
    public XdrTransportItf<VirRpcSvc> getTransport() throws IOException {
        return svc.connect(getAddress());
        
    }

    public IVirRpcCall getClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM,PROGVER);
    }

    private IVirRpcCall createClient(int prognum, int progver) throws IOException {
        client =  VirRpcSvcBuilder.getImpl()
                .withTCP()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .build();
        client.start();
         XdrTransportItf<VirRpcSvc> t = client.connect(svc.getInetSocketAddress(IpProtocolType.TCP));
        return IVirRpcCall.getImpl(prognum, progver, null, t);
    }
    
    public void close() throws IOException{
        close(client);
        close(svc);
    }

    private void close(VirRpcSvc svc) throws IOException {
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

    public IVirRpcCall getBadProgClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM+12,PROGVER);
    }

    public int getAnyProc() {
        return 0;
    }

    public IVirRpcCall getBadVersionClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM,PROGVER+12);
    }
    
}
