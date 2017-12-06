package org.dcache.utils;
import static org.junit.Assert.*;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.UnexpectedException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.XdrVoid;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.itf.RpcDispatchableItf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  class EmbeddedGenericServer
    <
        SVC_T extends RpcSvcItf<SVC_T,CALL_T>,
        CALL_T extends RpcCallItf<SVC_T,CALL_T>,
        BUILDER_T extends OncRpcSvcBuilderItf<SVC_T,CALL_T,BUILDER_T>
    >
    implements Closeable{
    
    private final static Logger _log = LoggerFactory.getLogger(EmbeddedGenericServer.class);
    
    private static final int PROGNUM = 536903814;
    private static final int PROGVER = 1;
    private static final int LIBVIRT_PORT = 16509;

    private final RpcDispatchableItf<SVC_T,CALL_T> unexpectedFailingAction = new RpcDispatchableItf<SVC_T,CALL_T>(){

        @Override
        public void dispatchOncRpcCall(CALL_T call) throws OncRpcException, IOException {
            fail();
            
        }

    };
    private EmbbeddedGenericServerFactory<SVC_T,CALL_T,BUILDER_T> _factory;

    
    protected  RpcCallItf<SVC_T,CALL_T> createRpcCaller(int prognum, int progver, XdrTransportItf<SVC_T,CALL_T> transport) {
       return  _factory.createRpcCaller(prognum,progver,transport);
    }
    protected  BUILDER_T createOncSvcBuilder() {
        return _factory.createOncSvcBuilder();
    }
    
    
    
    Map<Integer,RpcDispatchableItf<SVC_T,CALL_T>> srcActions = new ConcurrentHashMap<>();

    RpcDispatchableItf<SVC_T,CALL_T> fakeSrvActions = (CALL_T call) ->
    {
        RpcDispatchableItf<SVC_T,CALL_T> action = srcActions.get(call.getProcedure());
        if (null != action){
            try {
                _log.info("Calling action for proc " + call.getProcedure());
                action.dispatchOncRpcCall(call);
            } catch(Exception e) {
                _log.warn("An exception occured while processing proc " + call.getProcedure() + " "+e.getMessage());
                processHighLevelException(call,e);
            }
        } else {
            _log.warn("Procedure #{} is not defined");
            call.failProcedureUnavailable();
        }
    };

    private  RpcSvcItf<SVC_T,CALL_T> svc;

    private  RpcSvcItf<SVC_T,CALL_T> client;

    
    //TODO LIBVIRT_PORT ??
    public EmbeddedGenericServer(EmbbeddedGenericServerFactory<SVC_T,CALL_T,BUILDER_T> factory) throws IOException{
        this(factory,LIBVIRT_PORT,null);
    }
    public EmbeddedGenericServer(EmbbeddedGenericServerFactory<SVC_T,CALL_T,BUILDER_T> factory, int port,OtherParams params) throws IOException {
        _factory = factory;
        add(0,(CALL_T call)-> call.reply(XdrVoid.XDR_VOID));
        BUILDER_T builder;
        if (null == params) {
            builder = createOncSvcBuilder();
        } else {
            builder = createOncSvcBuilder();
        }
                builder.withPort(port)
                .withWorkerThreadIoStrategy()
                .withRpcService(new OncRpcProgram(PROGNUM, PROGVER), fakeSrvActions);
        svc = builder.build();
        System.out.println(svc.getClass());
        svc.start();
    }
    
    private void add(int procNumber, RpcDispatchableItf<SVC_T,CALL_T> action) {
        srcActions.put(procNumber,action );
        System.out.println(srcActions.size());
    }
    private InetSocketAddress getAddress() {
        return svc.getInetSocketAddress(IpProtocolType.TCP);
    }
    public int getListeningPort() {
        return svc.getInetSocketAddress(IpProtocolType.TCP).getPort();
    }
    
    public XdrTransportItf<SVC_T,CALL_T> getTransport() throws IOException {
        return svc.connect(getAddress());
        
    }

    public RpcCallItf<SVC_T,CALL_T> getClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM,PROGVER);
    }

    private RpcCallItf<SVC_T,CALL_T> createClient(int prognum, int progver) throws IOException {
        client = createOncSvcBuilder()
                .withClientMode()
                .withWorkerThreadIoStrategy()
                .build();
        client.start();
        XdrTransportItf<SVC_T,CALL_T> t = client.connect(svc.getInetSocketAddress(IpProtocolType.TCP));
        return createRpcCaller(prognum, progver, t);
    }
    
    public void close() throws IOException{
        close(client);
        close(svc);
    }

    private void close(RpcSvcItf<SVC_T,CALL_T> client2) throws IOException {
        if (client2 != null) client2.stop();
    }
    //TODO Modifier le nom des variables (available est ambigu
    public int getUnavailableProc() {
        Random rand = new Random();
        int randomNum = rand.nextInt();
        while (isAvailbale(randomNum)){
            randomNum = (int)Math.random();
            System.out.println(randomNum);
        }
        return randomNum;
    }

    private boolean isAvailbale(int randomNum) {
        return srcActions.keySet().contains(randomNum);
        //return null != srcActions.get(randomNum);
    }

    public int unexpectedErrorCall() {
        int numberProc = getUnavailableProc();
        add(numberProc,unexpectedFailingAction);
        return numberProc;
    }
    
    public RpcCallItf<SVC_T,CALL_T> getBadProgClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM+12,PROGVER);
    }

    public int getAnyProc() {
        return Integer.MAX_VALUE;
    }

    public RpcCallItf<SVC_T,CALL_T> getBadVersionClientCall() throws IOException {
        if (null != client) throw new UnexpectedException("A client already exists");
        return createClient(PROGNUM,PROGVER+12);
    }

    protected void processHighLevelException(RpcCallItf<SVC_T,CALL_T> call, Exception e){
       throw new RuntimeException(e);
    }
    //TODO Interface duplicated with AbstractOncRpcClient
    public interface OtherParams {

    }
    
}
