package org.dcache.xdr.model.root;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.ProtocolFactoryItf;
import org.dcache.xdr.model.itf.RpcDispatchableItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractRpcProtocolFactory<SVC_T extends RpcSvcItf<SVC_T>> implements ProtocolFactoryItf<SVC_T>{
    private final static Logger _log = LoggerFactory.getLogger(AbstractRpcProtocolFactory.class);
    private Map<OncRpcProgram, RpcDispatchableItf<SVC_T>> _programs;
    private RpcSvcItf<SVC_T> _svc;
    
    public AbstractRpcProtocolFactory(){
        try {
            _log.info("Creating a default protocol factory for type {}", getGenericSVC());
        } catch (NoSuchFieldException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private Type getGenericSVC() throws NoSuchFieldException, SecurityException {
         Field field = AbstractRpcProtocolFactory.class. getDeclaredField ("_svc");
         Type type   = field. getGenericType ();
         return type;
    }

    protected Map<OncRpcProgram, RpcDispatchableItf<SVC_T>> getPrograms() {
        return _programs;
    }
    
    void setSvc(RpcSvcItf<SVC_T> svc){
        _svc = svc;
    }
    
    protected RpcSvcItf<SVC_T> getSvc() {
        return _svc;
    }
    @Override
    public HeaderItf decode(Xdr xdr) throws OncRpcException, IOException {
        return new AbstractRpcMessage(xdr);
    }
    @Override
    public void processBuilder(OncRpcSvcBuilderItf<SVC_T> builder) {
    }
    @Override
    public void preStopActions(RpcSvcItf<SVC_T> rpcSvcItf) throws IOException {}
    
    @Override
    public void doPreStartAction(RpcSvcItf<SVC_T> rpcSvcItf) throws IOException {}
}
