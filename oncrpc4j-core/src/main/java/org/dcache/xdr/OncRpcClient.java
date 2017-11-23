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
import static org.dcache.utils.ConversionUtils.helperCAST;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.model.impl.AbstractGrizzlyXdrTransport;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractOncRpcClient;

public class OncRpcClient extends AbstractOncRpcClient<OncRpcSvc> implements IOncRpcClient{

    public OncRpcClient(InetAddress address, int protocol, int port) {
        super(address, protocol, port);
        // TODO Auto-generated constructor stub
    }

    public OncRpcClient(InetAddress address, int protocol, int port, int localPort) {
        this(new InetSocketAddress(address, port), protocol, localPort, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }

    public OncRpcClient(InetAddress address, int protocol, int port, int localPort, IoStrategy ioStrategy) {
        this(new InetSocketAddress(address, port), protocol, localPort, ioStrategy, DEFAULT_SERVICE_NAME);
    }

    public OncRpcClient(InetAddress address, int protocol, int port, int localPort, IoStrategy ioStrategy, String serviceName) {
        this(new InetSocketAddress(address, port), protocol, localPort, ioStrategy, serviceName);
    }

    public OncRpcClient(InetSocketAddress socketAddress, int protocol) {
        this(socketAddress, protocol, 0, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }

    public OncRpcClient(InetSocketAddress socketAddress, int protocol, int localPort, IoStrategy ioStrategy, String serviceName) {
        super(socketAddress,protocol,localPort,ioStrategy,serviceName);
    }
    
    @Override
    protected IOncRpcSvcBuilder getRpcSvcBuilder() {
        return  IOncRpcSvcBuilder.getImpl().withTCP().withUDP();
    }
    
    @Override
    protected IOncRpcSvcBuilder getRpcSvcBuilder(int protocol) {
        return IOncRpcSvcBuilder.getImpl().withIpProtocolType(protocol);
    }
    @Override
    public   XdrTransport  connect() throws IOException {
        return   helperCAST(super.connect());
    }
}
