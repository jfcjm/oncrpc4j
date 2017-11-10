/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.dcache.xdr.model.itf.GenItfOncRpcSvcBuilder;
import org.dcache.xdr.model.itf.GenItfRpcSvc;
import org.dcache.xdr.model.itf.GenItfXdrTransport;
import org.dcache.xdr.model.itf.GenXdrTransport;
import org.dcache.xdr.model.root.GenAbstractOncRpcClient;

public class GenOncRpcClient extends GenAbstractOncRpcClient<GenOncRpcSvc> {

    public GenOncRpcClient(InetAddress address, int protocol, int port) {
        this(new InetSocketAddress(address, port), protocol, 0, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }

    public GenOncRpcClient(InetAddress address, int protocol, int port, int localPort) {
        this(new InetSocketAddress(address, port), protocol, localPort, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }

    public GenOncRpcClient(InetAddress address, int protocol, int port, int localPort, IoStrategy ioStrategy) {
        this(new InetSocketAddress(address, port), protocol, localPort, ioStrategy, DEFAULT_SERVICE_NAME);
    }

    public GenOncRpcClient(InetAddress address, int protocol, int port, int localPort, IoStrategy ioStrategy, String serviceName) {
        this(new InetSocketAddress(address, port), protocol, localPort, ioStrategy, serviceName);
    }

    public GenOncRpcClient(InetSocketAddress socketAddress, int protocol) {
        this(socketAddress, protocol, 0, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }

    public GenOncRpcClient(InetSocketAddress socketAddress, int protocol, int localPort, IoStrategy ioStrategy, String serviceName) {
        super(socketAddress,protocol,localPort,ioStrategy,serviceName);
    }
    
    @Override
    protected GenItfOncRpcSvcBuilder<GenOncRpcSvc> getRpcSvcBuilder() {
        return new GenOncRpcSvcBuilder().withTCP().withUDP();
    }
    
    @Override
    protected GenItfOncRpcSvcBuilder<GenOncRpcSvc> getRpcSvcBuilder(int protocol) {
        return new GenOncRpcSvcBuilder().withIpProtocolType(protocol);
    }
    @Override
    public  GenXdrTransport<GenOncRpcSvc> connect() throws IOException {
        return (GenXdrTransport<GenOncRpcSvc>) super.connect();
    }
}
