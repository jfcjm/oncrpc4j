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
package org.dcache.xdr.impl;

import java.io.IOException;

import org.dcache.xdr.BadXdrOncRpcException;
import org.dcache.xdr.MismatchInfo;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.RpcAccepsStatus;
import org.dcache.xdr.RpcAuthVerifier;
import org.dcache.xdr.RpcRejectStatus;
import org.dcache.xdr.RpcReply;
import org.dcache.xdr.RpcReplyStatus;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RpcReplyImpl extends AbstractRpcReply<OncRpcSvc>  implements RpcReply{

    @SuppressWarnings("unused")
    private static final Logger _log = LoggerFactory.getLogger(RpcReply.class);

    public RpcReplyImpl(int xid, Xdr xdr, XdrTransportItf<OncRpcSvc> transport) throws OncRpcException, IOException {
        super(xid,xdr,transport);
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfRpcReply#getRejectStatus()
     */
    @Override
    public int getRejectStatus() {
        if (isAccepted()) {
            throw new IllegalStateException("Message is accepted");
        }
        return _rejectStatus;
    }

    @Override
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

    @Override
    protected int getReplyStatus(Xdr xdr) throws BadXdrOncRpcException {
        return xdr.xdrDecodeInt();
    }

    @Override
    public  Integer getError() {
        return null;
    }
    
}
