package org.dcache.generics.alt;

import static org.junit.Assert.*;

import java.io.IOException;

import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcDispatchableItf;
import org.dcache.xdr.model.root.AbstractRpcCall;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.XdrVoid;
import org.junit.Test;
/**
 * We would like tonot modify the original core source code. FOr this we need to
 * be able to write things such as (OncRpcCall call) -> call.reply(XdrVoid.XDR_VOID)
 * for an instanciatinn of OncRpcCall AbstractRpcCall.
 * 
 * The initial situation is in test1 -- The solution is given on test3; : add RPCCALL_T type parameter
 * to dispatchable
 * @author jmk
 *
 */
public class TestRpcDispatchable {
    class  RpcCallAlt extends AbstractRpcCall<OncSvcAlt>{

        public RpcCallAlt(AbstractRpcCall<OncSvcAlt> call) {
            super(call);
        }
        
    }
    @Test
    public void test() {
        RpcDispatchableItf<OncSvcAlt> d1 = call -> call.reply(XdrVoid.XDR_VOID);
        d1 = new RpcDispatchableItf<OncSvcAlt>() {

            @Override
            public void dispatchOncRpcCall(RpcCallItf<OncSvcAlt> call) throws OncRpcException, IOException {
                // TODO Auto-generated method stub
                
            }
            
        };
    }
    @Test
    public void test2() {
        RpcDispatchableAltItf<OncSvcAlt,RpcCallItf<OncSvcAlt>> d2 = new RpcDispatchableAltItf<OncSvcAlt,RpcCallItf<OncSvcAlt>>() {

            @Override
            public void dispatchOncRpcCall(RpcCallItf<OncSvcAlt> call) throws OncRpcException, IOException {
                // TODO Auto-generated method stub
                
            }
            
        };
        d2 =  call -> call.reply(XdrVoid.XDR_VOID);
    }
    
    @Test
    public void test3() {
        
        RpcDispatchableAltItf<OncSvcAlt,RpcCallAlt> d3 = new RpcDispatchableAltItf<OncSvcAlt,RpcCallAlt> (){

            @Override
            public void dispatchOncRpcCall(RpcCallAlt call) throws OncRpcException, IOException {
                // TODO Auto-generated method stub
                
            }
            
        };
    }

}
