/*
 * Copyright (c) 2009 - 2016 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.dcache.xdr;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenRpcReply<SVC_T extends GenRpcSvc<SVC_T>> {

    private final static Logger _log = LoggerFactory.getLogger(GenRpcReply.class);
    /**
     * XID of corresponding request
     */
    private final int _xid;
    /**
     * XDR message
     */
    private final Xdr _xdr;
    protected int _replyStatus;
    private int _acceptedStatus;
    private int _rejectStatus;
    private MismatchInfo _mismatchInfo;
    private int _authStatus;

    private RpcAuthVerifier _verf;
    private final GenXdrTransport<SVC_T> _transport;

    public GenRpcReply(int xid, Xdr xdr, GenXdrTransport<SVC_T> transport) throws OncRpcException, IOException {
        _xid = xid;
        _xdr = xdr;
        _transport = transport;

        // decode
        _replyStatus = getReplyStatus(xdr);
        processReplyStatus(_replyStatus,xdr);
    }

    public boolean isAccepted() {
        return _replyStatus == RpcReplyStatus.MSG_ACCEPTED;
    }

    public int getAcceptStatus() {
        if (!isAccepted()) {
            throw new IllegalStateException("Message in not accepted");
        }

        return _acceptedStatus;
    }

    public MismatchInfo getMismatchInfo() {
        return _mismatchInfo;
    }

    public int getAuthStatus() {
        return _authStatus;
    }

    public int getRejectStatus() {
        if (isAccepted()) {
            throw new IllegalStateException("Message is accepted");
        }
        return _rejectStatus;
    }

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

    protected void processReplyStatus(int _replyStatus, Xdr xdr) throws OncRpcException, IOException {
        switch (_replyStatus) {
        case RpcReplyStatus.MSG_ACCEPTED:
            _verf = new RpcAuthVerifier(xdr);
            _acceptedStatus = xdr.xdrDecodeInt();
            switch (_acceptedStatus) {
                case RpcAccepsStatus.PROG_MISMATCH:
                    _mismatchInfo = new MismatchInfo();
                    _mismatchInfo.xdrDecode(xdr);
            }
            break;

        case RpcReplyStatus.MSG_DENIED:
            _rejectStatus = xdr.xdrDecodeInt();
            switch (_rejectStatus) {
                case RpcRejectStatus.RPC_MISMATCH:
                    _mismatchInfo = new MismatchInfo();
                    _mismatchInfo.xdrDecode(xdr);
                    break;
                case RpcRejectStatus.AUTH_ERROR:
                    _authStatus = xdr.xdrDecodeInt();
                    break;
            }
            break;
        default:
        // FIXME: ERROR CODE HERE
    }
        
    }

    protected int getReplyStatus(Xdr xdr) throws BadXdrOncRpcException {
        return xdr.xdrDecodeInt();
    }
    
}
