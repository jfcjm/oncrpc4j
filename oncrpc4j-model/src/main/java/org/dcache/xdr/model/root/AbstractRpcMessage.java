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
    
  //appel de protocolfilter when receiving a message
    public AbstractRpcMessage(Xdr xdr) throws OncRpcException, IOException {
            minimalDecode(xdr);
        
    }
    public AbstractRpcMessage(int prog, int ver, int procedure, RpcAuth cred) {
    	this(0,0,0,prog,ver,procedure,null,null,cred);
	}
    
    // Call when doing an initial call
    // ne fonctionne que si utilisé pour un appel
    private AbstractRpcMessage(int xid, int type,  int rpcvers, int prog, int version,
            int proc, XdrAble args, RpcAuth auth, RpcAuth cred) {
        _xid = xid;
        _messageType = 	RpcMessageType.CALL;
        _rpcvers = 	rpcvers;
        _prog = prog;
        _version = version;
        _proc = proc;
        _cred = (null != auth ) ? auth :cred;
        _args = args;
    }

	@Override
	public void update(int xid, int mType, int rpcvers, int procedure, RpcAuth auth,XdrAble args) {
		_xid = xid;
		_messageType = mType;
		_rpcvers = rpcvers;
		_proc = procedure;
		if (null != auth ){
			_cred = auth;
		}
		_args = args;
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

    @Override
    public void decodeAsReply(XdrDecodingStream xdr) throws OncRpcException, IOException {
        xdrDecode(xdr);
        
    }
    
    @Override
    public void xdrEncodeAsCall(Xdr xdr) throws OncRpcException, IOException {
    	xdr.beginEncoding();
        xdrEncode(xdr);
        xdr.endEncoding();
    }
    
    @Override
    public void encodeAsAcceptedReply(XdrEncodingStream xdr,int state, XdrAble reply) throws OncRpcException, IOException {
        minimalEncode(xdr);
        xdr.xdrEncodeInt(RpcReplyStatus.MSG_ACCEPTED);
        _cred.getVerifier().xdrEncode(xdr);
        xdr.xdrEncodeInt(state);
        reply.xdrEncode(xdr);
    }
    
    @Override
    public void encodeAsReject(XdrEncodingStream xdr,  int status, XdrAble reason) throws OncRpcException, IOException {
        minimalEncode(xdr);
        xdr.xdrEncodeInt(RpcReplyStatus.MSG_DENIED);
        xdr.xdrEncodeInt(status);
        reason.xdrEncode(xdr);
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
    public void asReply() {
        //_rpcMessage.setType(RpcMessageType.REPLY);    
        _messageType = RpcMessageType.REPLY;
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
    public int getMessageType() {
        return _messageType;
    }

    @Override
    public int getXid() {
        // TODO Auto-generated method stub
        return _xid;
    }
}
