/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
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

import org.dcache.xdr.BadXdrOncRpcException;
import org.dcache.xdr.RpcMessage;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirRpcMessage extends RpcMessage {
	private final static Logger _log = LoggerFactory.getLogger(VirRpcMessage.class);

    VirRpcMessage(XdrDecodingStream xdr) throws BadXdrOncRpcException {
        super(xdr);
        _log.info("Creating a new VirRpcMessage");
    }

    public VirRpcMessage(int xid, int type) {
        super(xid,type);
    }
    @Override
    public void xdrDecode(XdrDecodingStream xdr) throws BadXdrOncRpcException {
    	// in virt rpc xid and type are not at the first position,
        // we skip the first three int and then we'll rewind the buffer
        _log.debug("decoding");
        int _program = xdr.xdrDecodeInt();
        int _version = xdr.xdrDecodeInt();
        int _procedure = xdr.xdrDecodeInt();
        _log.debug("program {}, version {}, procedure {}",_program,_version,_procedure);

        _type = xdr.xdrDecodeInt();
        _xid = xdr.xdrDecodeInt();
        _log.debug("type : {}, xid: {}",type(),xid());
        xdr.beginDecoding();
    }
    //TODO 
    @Override
    public void xdrEncode(XdrEncodingStream xdr) {
        super.xdrEncode(xdr);
        /*
         * supêr fait
        xdr.xdrEncodeInt(_xid);
        xdr.xdrEncodeInt(_type);
        */
    }
}
