package org.dcache.xdr.model.root;

import java.io.IOException;

import org.dcache.xdr.BadXdrOncRpcException;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcAuthTypeNone;
import org.dcache.xdr.RpcCredential;
import org.dcache.xdr.RpcMessageType;
import org.dcache.xdr.RpcMismatchReply;
import org.dcache.xdr.RpcReplyStatus;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.RpcSvcItf;

public class AbstractRpcMessage<SVC_T extends RpcSvcItf<SVC_T>> implements  HeaderItf<SVC_T> {

    private static final int RPCVERS = 2;
    
    // field replacing rpcmessage
    private int _xid;
    private int _messageType;
    
    private int _rpcvers;
    private int _prog;
    private int _version;
    private int _proc;
    private RpcAuth _cred;
    private XdrAble _args;
  //appel de protcolfilter
    public AbstractRpcMessage(Xdr xdr) throws OncRpcException, IOException {
            minimalDecode(xdr);
        
    }


    /**
     * Replace RpcmessageEncoding
     * @param xdr
     */
    private void minimalEncode(XdrEncodingStream xdr) {
        xdr.xdrEncodeInt(_xid);
        xdr.xdrEncodeInt(_messageType);
    }


    /**
     * Replace Rpcmessage decoding
     * @param xdr
     */
    private void minimalDecode(XdrDecodingStream xdr) throws BadXdrOncRpcException {
         _xid = xdr.xdrDecodeInt();
         _messageType = xdr.xdrDecodeInt();
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
    public AbstractRpcMessage(int xid, int prog, int ver, int proc, RpcAuth cred) {
        _xid = xid;
        _messageType = RpcMessageType.CALL;
        _prog = prog;
        _version = ver;
        _proc = proc;
        _cred = cred;
    }
    // Call when doing an initial call
    // ne fonctionne que si utilisé pour un appel
    public AbstractRpcMessage(int xid, int type,  int rpcvers2, int _prog2, int _version2,
            int procedure, XdrAble args, RpcAuth auth, RpcAuth cred2) {
        this(xid,_prog2,_version2,procedure,(null != auth) ? auth : cred2);
        // xid super 
        // type is akready set by super
        // _prog2 super
        // _version2 super 
        //auth super
        //cred2super 
        // procedure super
        // rpcvers2 OK
        // args OK
        _args = args;
        _rpcvers = rpcvers2;
        
        
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
        minimalDecode(xdr);
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
        minimalEncode(xdr);
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
        return _messageType;
    }

    @Override
    public int getXid() {
        // TODO Auto-generated method stub
        return _xid;
    }

    @Override
    public void asReply() {
        //_rpcMessage.setType(RpcMessageType.REPLY);    
        _messageType = RpcMessageType.REPLY;
    }

    @Override
    public void encodeAsReject(XdrEncodingStream xdr,  int status, XdrAble reason) throws OncRpcException, IOException {
        /*
        xdr.xdrEncodeInt(RpcReplyStatus.MSG_DENIED);
        xdr.xdrEncodeInt(status);
        reason.xdrEncode(_xdr);
        */
        //_rpcMessage.xdrEncode(xdr);
        
        minimalEncode(xdr);
        xdr.xdrEncodeInt(RpcReplyStatus.MSG_DENIED);
        xdr.xdrEncodeInt(status);
        reason.xdrEncode(xdr);
    }

    @Override
    public void encodeAsAcceptedReply(XdrEncodingStream xdr,int state, XdrAble reply) throws OncRpcException, IOException {
        minimalEncode(xdr);
        xdr.xdrEncodeInt(RpcReplyStatus.MSG_ACCEPTED);
        _cred.getVerifier().xdrEncode(xdr);
        xdr.xdrEncodeInt(state);
        reply.xdrEncode(xdr);
        
    }
    
}
