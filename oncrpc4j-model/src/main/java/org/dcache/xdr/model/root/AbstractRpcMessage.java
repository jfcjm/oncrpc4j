package org.dcache.xdr.model.root;

import java.io.IOException;

import org.dcache.xdr.BadXdrOncRpcException;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcAuthTypeNone;
import org.dcache.xdr.RpcCredential;
import org.dcache.xdr.RpcMessage;
import org.dcache.xdr.RpcMessageType;
import org.dcache.xdr.RpcMismatchReply;
import org.dcache.xdr.RpcReplyStatus;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.RpcSvcItf;

public class AbstractHeader<SVC_T extends RpcSvcItf<SVC_T>> implements  HeaderItf<SVC_T> {

    private static final int RPCVERS = 2;
    private int _rpcvers;
    private int _prog;
    private int _version;
    private int _proc;
    private RpcAuth _cred;
    private RpcMessage _rpcMessage;
    private XdrAble _args;


    public AbstractHeader(Xdr xdr) throws OncRpcException, IOException {
        this(true,xdr);
    }
    
    public AbstractHeader(RpcMessage rpcMessage, int rpcvers, int prog, int version, int proc, XdrAble args,RpcAuth auth, RpcAuth cred) {
        _rpcMessage = rpcMessage;
        _rpcvers = rpcvers;
        _prog = prog;
        _version = version;
        _proc = proc;
        _cred = (null != auth) ? auth : cred;
        _args = args;
    }
  //appel de protcolfilter
    public AbstractHeader(boolean shouldDecode, Xdr xdr) throws OncRpcException, IOException {
        System.out.println("Should decoe : " + shouldDecode);
        if(shouldDecode) {
            xdrFullDecode(xdr);
            // call from RcCall : we decode
        } else {
            _rpcMessage = new RpcMessage(xdr);
        }
        
    }

    /**
     * Appel en venant de gss , temporaire on remlit le message
     * @param xid
     * @param prog
     * @param ver
     * @param proc
     * @param cred
     * @param xdr
     */
    public AbstractHeader(int xid, int prog, int ver, int proc, RpcAuth cred) {
        _rpcMessage = new RpcMessage(xid,RpcMessageType.CALL); // on sait que c'est un appel
        _prog = prog;
        _version = ver;
        _proc = proc;
        _cred = cred;
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.model.root.HeaderItf2#getRpcVers()
     */
    @Override
    public int getRpcVers() {
        return _rpcvers;
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.model.root.HeaderItf2#getProg()
     */
    @Override
    public int getProg() {
        return _prog;
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.model.root.HeaderItf2#getVersion()
     */
    @Override
    public int getVersion() {
        return _version;
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.model.root.HeaderItf2#getProc()
     */
    @Override
    public int getProc() {
        return _proc;
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.model.root.HeaderItf2#getCredential()
     */
    @Override
    public  RpcAuth getCredential() {
        return _cred;
    }
    


    @Override
    public void decodeAsReply(XdrDecodingStream xdr) throws OncRpcException, IOException {
        xdrDecode(xdr);
        
    }
    
    private void xdrFullDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        _rpcMessage =  new RpcMessage(xdr);
        xdrDecode(xdr);
    }
    @Override
    public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        _rpcvers = xdr.xdrDecodeInt();
        System.out.println(_rpcvers);
        if (_rpcvers != RPCVERS) {
           throw new RpcMismatchReply(_rpcvers, 2);
        }

       _prog = xdr.xdrDecodeInt();
       _version = xdr.xdrDecodeInt();
       _proc = xdr.xdrDecodeInt();
       _cred = RpcCredential.decode(xdr);

    }

    @Override
    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
        _rpcMessage.xdrEncode(xdr);
        xdr.xdrEncodeInt(RPCVERS);
        xdr.xdrEncodeInt(_prog);
        xdr.xdrEncodeInt(_version);
        xdr.xdrEncodeInt(_proc);
        _cred.xdrEncode(xdr);
        _args.xdrEncode(xdr);;
    }

    @Override
    public void xdrEncodeAsCall(Xdr xdr) throws OncRpcException, IOException {
        xdrEncode(xdr);
    }

    @Override
    public int getMessageType() {
        return _rpcMessage.type();
    }

    @Override
    public int getXid() {
        // TODO Auto-generated method stub
        return _rpcMessage.xid();
    }

    @Override
    public void asReply() {
        _rpcMessage.setType(RpcMessageType.REPLY);    
    }

    @Override
    public void encodeAsReject(XdrEncodingStream xdr,  int status, XdrAble reason) throws OncRpcException, IOException {
        /*
        xdr.xdrEncodeInt(RpcReplyStatus.MSG_DENIED);
        xdr.xdrEncodeInt(status);
        reason.xdrEncode(_xdr);
        */
        _rpcMessage.xdrEncode(xdr);
        xdr.xdrEncodeInt(RpcReplyStatus.MSG_DENIED);
        xdr.xdrEncodeInt(status);
        reason.xdrEncode(xdr);
    }

    @Override
    public void encodeAsAcceptedReply(XdrEncodingStream xdr,int state, XdrAble reply) throws OncRpcException, IOException {
        /*
        replyMessage.xdrEncode(_xdr);
        xdr.xdrEncodeInt(RpcReplyStatus.MSG_ACCEPTED);
        _cred.getVerifier().xdrEncode(xdr);
        xdr.xdrEncodeInt(state);
        reply.xdrEncode(xdr);
        */
        _rpcMessage.xdrEncode(xdr);
        xdr.xdrEncodeInt(RpcReplyStatus.MSG_ACCEPTED);
        _cred.getVerifier().xdrEncode(xdr);
        xdr.xdrEncodeInt(state);
        reply.xdrEncode(xdr);
        
    }
    
}
