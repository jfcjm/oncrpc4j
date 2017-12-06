package org.dcache.xdr.model.itf;

import java.io.IOException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;
/**
 * This cable define header of rpc packets under a form suitable for
 * extension or modifications by alternatives implementations
 * @author jmk
 *
 */
public interface HeaderItf<SVC_T extends RpcSvcItf<SVC_T,CALL_T>,CALL_T extends RpcCallItf<SVC_T,CALL_T>> extends XdrAble{
    
    int getRpcVers();

    int getProg();

    int getVersion();

    int getProc();

    RpcAuth getCredential();

    int getMessageType();

    int getXid();

    void decodeAsReply(XdrDecodingStream xdr) throws OncRpcException, IOException;


    void asReply();

    void encodeAsReject(XdrEncodingStream xdr,  int status, XdrAble reason) throws OncRpcException, IOException;

    void encodeAsAcceptedReply(XdrEncodingStream xdr, int state, XdrAble reply)throws OncRpcException, IOException;

    void xdrEncodeAsCall(Xdr xdr) throws OncRpcException, IOException;
	void update(int xid, int call, int rpcvers, int procedure, RpcAuth auth, XdrAble args);

}