/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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

import java.net.InetAddress;
import java.util.concurrent.Future;

import org.dcache.xdr.model.itf.OncRpcClientItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.root.AbstractSimpleRpcClient;

public class SimpleRpcClient extends AbstractSimpleRpcClient<OncRpcSvc,RpcCall,XdrTransport, RpcReply>
{

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println("usage: SimpleRpcClient host port");
            System.exit(1);
        }
        new SimpleRpcClient().process(args);

	}

    @Override
    protected RpcCallItf<OncRpcSvc, RpcCall,XdrTransport, RpcReply> createRpcCall(int i, int j, RpcAuth auth,
            XdrTransport transport) {
        return new RpcCall(i,j,auth,transport);
    }

    @Override
    protected OncRpcClientItf<OncRpcSvc, RpcCall,XdrTransport, RpcReply> createRpcClient(InetAddress address, int tcp, int port) {
        return new OncRpcClient(address,tcp,port);
    }

    @Override
    protected AbstractSimpleRpcClient<OncRpcSvc, RpcCall,XdrTransport, RpcReply> createSimpleClient() {
        return null;
    }
}
