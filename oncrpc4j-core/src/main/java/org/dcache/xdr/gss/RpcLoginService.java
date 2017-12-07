package org.dcache.xdr.gss;


import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.RpcReply;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.model.itf.RpcLoginServiceItf;

public interface RpcLoginService extends RpcLoginServiceItf<OncRpcSvc,RpcCall,XdrTransport, RpcReply> {

}
