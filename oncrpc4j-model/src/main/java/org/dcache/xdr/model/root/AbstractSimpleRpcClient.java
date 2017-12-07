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
package org.dcache.xdr.model.root;

import java.net.InetAddress;
import java.util.concurrent.Future;

import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcAuthTypeNone;
import org.dcache.xdr.XdrVoid;
import org.dcache.xdr.model.itf.OncRpcClientItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
//TODO à génériser
public abstract class AbstractSimpleRpcClient<
    SVC_T extends RpcSvcItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
    CALL_T extends RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
    TRANSPORT_T extends XdrTransportItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
    REPLY_T extends RpcReplyItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>> {
    protected void process (String[] args) throws Exception {
        InetAddress address = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);
        OncRpcClientItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> rpcClient = createRpcClient(address,IpProtocolType.TCP,port);
        TRANSPORT_T transport = rpcClient.connect();
        
        RpcAuth auth = new RpcAuthTypeNone();

         RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> call = createRpcCall(100017, 1, auth, transport);
        /*
         * call PROC_NULL (ping)
         */
        call.call(0, XdrVoid.XDR_VOID, XdrVoid.XDR_VOID);

        Future<XdrVoid> r = call.call(0, XdrVoid.XDR_VOID, XdrVoid.class);
        r.get();
        rpcClient.close();
    }
    protected abstract RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> createRpcCall(int i, int j, RpcAuth auth, TRANSPORT_T transport);
    protected abstract OncRpcClientItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> createRpcClient(InetAddress address, int tcp, int port);
    protected abstract AbstractSimpleRpcClient<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> createSimpleClient();
}
