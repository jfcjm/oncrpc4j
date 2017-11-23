package org.dcache.xdr.model.root;

import java.io.IOException;

import org.dcache.xdr.BadXdrOncRpcException;
import org.dcache.xdr.MismatchInfo;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAccepsStatus;
import org.dcache.xdr.RpcAuthStat;
import org.dcache.xdr.RpcAuthVerifier;
import org.dcache.xdr.RpcRejectStatus;
import org.dcache.xdr.RpcReplyStatus;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRpcReply<SVC_T extends RpcSvcItf<SVC_T>> implements RpcReplyItf<SVC_T>{

    private static final Logger _log = LoggerFactory.getLogger(AbstractRpcReply.class);

    protected abstract int getReplyStatus(Xdr xdr) throws BadXdrOncRpcException;

    protected abstract void processReplyStatus(int _replyStatus, Xdr xdr) throws OncRpcException, IOException;

    public abstract int getRejectStatus();

    /**
     * XID of corresponding request
     */
    protected final int _xid;
    /**
     * XDR message
     */
    protected final Xdr _xdr;
    protected final XdrTransportItf<SVC_T> _transport;
    
    protected int _replyStatus;
    protected int _acceptedStatus;
    protected int _rejectStatus;
    protected MismatchInfo _mismatchInfo;
    protected int _authStatus;
    protected RpcAuthVerifier _verf;
    public AbstractRpcReply(int xid, Xdr xdr, XdrTransportItf<SVC_T> transport) throws OncRpcException, IOException {
        super();
        _xid = xid;
        _xdr = xdr;
        _transport = transport;

        // decode
        _replyStatus = getReplyStatus(xdr);
        processReplyStatus(_replyStatus,xdr);
    }

    @Override
    public boolean isAccepted() {
        return _replyStatus == RpcReplyStatus.MSG_ACCEPTED;
    }

    @Override
    public int getAcceptStatus() {
        if (!isAccepted()) {
            throw new IllegalStateException("Message in not accepted");
        }
    
        return _acceptedStatus;
    }

    @Override
    public MismatchInfo getMismatchInfo() {
        return _mismatchInfo;
    }

    @Override
    public int getAuthStatus() {
        return _authStatus;
    }

    @Override
    public void getReplyResult(XdrAble result) throws OncRpcException, IOException {  	
        _log.debug("decoding result class {}", result.getClass().getName());
        result.xdrDecode(_xdr);
        _xdr.endDecoding();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
    
        sb.append("xid: ").append(_xid);
        sb.append(" Status: ").append(RpcReplyStatus.toString(_replyStatus));
        if( _replyStatus == RpcReplyStatus.MSG_ACCEPTED) {
            sb.append(" AccespStatus: ").append(RpcAccepsStatus.toString(_acceptedStatus));
            if(_acceptedStatus == RpcAccepsStatus.PROG_MISMATCH) {
                sb.append(" :").append(_mismatchInfo);
            }
        }else{
            sb.append(" RejectStatus: ").append(RpcRejectStatus.toString(_rejectStatus));
            if(_rejectStatus == RpcRejectStatus.AUTH_ERROR){
                sb.append(" AuthError: ").append(RpcAuthStat.toString(_authStatus));
            }
    
        }
    
        return sb.toString();
    }

}