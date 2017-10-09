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
package org.libvirt;

import java.io.IOException;

import org.dcache.xdr.BadXdrOncRpcException;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcReply;
import org.dcache.xdr.RpcReplyStatus;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirRpcReply extends RpcReply{

    private final static Logger _log = LoggerFactory.getLogger(VirRpcReply.class);
    
    private int _errorDomain;
    private String _errorString;
    private remote_error _error;
   

    public VirRpcReply(int xid, Xdr xdr, XdrTransport transport) throws OncRpcException, IOException {
        super(xid,xdr,transport);
        _log.debug("reply status: {}",_replyStatus);
    }
    
    @Override
    protected void processReplyStatus(int _replyStatus, Xdr xdr) throws OncRpcException, IOException {

        switch (_replyStatus) {
            case RpcReplyStatus.MSG_ACCEPTED :
                
                /*JMK _verf = new RpcAuthVerifier(xdr);
               _acceptedStatus = xdr.xdrDecodeInt();
                _acceptedStatus = RpcAccepsStatus.SUCCESS;
                switch (_acceptedStatus) {
                    case RpcAccepsStatus.PROG_MISMATCH:
                        _mismatchInfo = new MismatchInfo();
                        _mismatchInfo.xdrDecode(xdr);
                }*/
                break;

            case RpcReplyStatus.MSG_DENIED:
                 _error = new remote_error(xdr);
                
                _log.debug("reply is an error. error :{}",_error);
                /*
                switch (_rejectStatus) {
                    case RpcRejectStatus.RPC_MISMATCH:
                        _mismatchInfo = new MismatchInfo();
                        _mismatchInfo.xdrDecode(xdr);
                        break;
                    case RpcRejectStatus.AUTH_ERROR:
                        _authStatus = xdr.xdrDecodeInt();
                        break;
                }*/
                break;
            default:
            // FIXME: ERROR CODE HERE
        }
    }
    @Override
    protected int getReplyStatus(Xdr xdr) throws BadXdrOncRpcException {
        int p = xdr.xdrDecodeInt();//program
        int v = xdr.xdrDecodeInt(); //version
        int proc = xdr.xdrDecodeInt();//procedure
        int t = xdr.xdrDecodeInt(); // type
        int s = xdr.xdrDecodeInt(); // serial
        int result = xdr.xdrDecodeInt();
        _log.debug("p {}, v {} , proc {}, t {}; s {}, status {}", p,v,proc,t,s,result);
        return result;
    }


    @Override
    public int getRejectStatus(){
        _log.warn(_error.toString());
        if (isAccepted()) {
            throw new IllegalStateException("Message is accepted");
        }
        return _error.getCode();
    }
    
    public remote_error getError(){
        return _error;
    }
    
    
    
    
    
    /*
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
    }*/
}
