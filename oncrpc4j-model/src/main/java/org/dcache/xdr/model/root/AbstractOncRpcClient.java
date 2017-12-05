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
package org.dcache.xdr.model.root;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.model.itf.OncRpcClientItf;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractOncRpcSvc;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;
import org.dcache.xdr.model.root.AbstractOncRpcClient.OtherParams;

public abstract class AbstractOncRpcClient<SVC_T extends RpcSvcItf<SVC_T>,BUILDER_T extends OncRpcSvcBuilderItf<SVC_T,BUILDER_T>> implements AutoCloseable, OncRpcClientItf<SVC_T> {

    protected static final String DEFAULT_SERVICE_NAME = null;

    private final InetSocketAddress _socketAddress;
    private final SVC_T _rpcsvc;
    /**
     * On a enleve le paramètre int protocol, de l'appel pour le moment
     * @param address
     * @param port
     */
    public AbstractOncRpcClient(InetAddress address,  int port) {
        this(new InetSocketAddress(address, port), 0, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }
    /**
     * On a enleve le paramètre int protocol, de l'appel pour le moment
     * @param address
     * @param port
     * @param localPort
     */
    public AbstractOncRpcClient(InetAddress address,  int port, int localPort) {
        this(new InetSocketAddress(address, port), localPort, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }
    /**
     * On a enleve le paramètre int protocol, de l'appel pour le moment
     * @param address
     * @param port
     * @param localPort
     * @param ioStrategy
     */
    public AbstractOncRpcClient(InetAddress address,  int port, int localPort, IoStrategy ioStrategy) {
        this(new InetSocketAddress(address, port),  localPort, ioStrategy, DEFAULT_SERVICE_NAME);
    }
    /**
     * On a enleve le paramètre int protocol, de l'appel pour le moment
     * @param address
     * @param port
     * @param localPort
     * @param ioStrategy
     * @param serviceName
     */
    public AbstractOncRpcClient(InetAddress address,  int port, int localPort, IoStrategy ioStrategy, String serviceName) {
        this(new InetSocketAddress(address, port), localPort, ioStrategy, serviceName);
    }
    /**
     * On a enleve le paramètre int protocol, de l'appel pour le moment
     * @param socketAddress
     * @param protocol
     */
    public AbstractOncRpcClient(InetSocketAddress socketAddress, int protocol) {
        this(socketAddress,  0, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }
    //JMK : type cast
    
    public AbstractOncRpcClient(InetSocketAddress socketAddress,  int localPort, IoStrategy ioStrategy, String serviceName) {
        this(socketAddress,localPort,ioStrategy,serviceName,null);
    }
    
    
    /**
     * On a enleve le paramètre int protocol, de l'appel pour le moment
     * @param socketAddress
     * @param localPort
     * @param ioStrategy
     * @param serviceName
     */
    public AbstractOncRpcClient(InetSocketAddress socketAddress,  int localPort, IoStrategy ioStrategy, String serviceName,OtherParams params) {
        _socketAddress = socketAddress;
        BUILDER_T builder ;
        if (null == params) {
            System.out.println("=========builder: "+1);
            builder = getRpcSvcBuilder();
        } else {
            System.out.println("=========builder: "+2);
            builder = getRpcSvcBuilder(params);
        }
        System.out.println("=========builder: "+ builder);
        _rpcsvc = builder
                .withClientMode()
                .withPort(localPort)
                //TODO JMK .withIpProtocolType(protocol)
                .withIoStrategy(ioStrategy)
                .withServiceName(serviceName)
                .build(); 
    }
    public XdrTransportItf<SVC_T> connect() throws IOException {
        return connect(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public XdrTransportItf<SVC_T> connect(long timeout, TimeUnit timeUnit) throws IOException {
        XdrTransportItf<SVC_T> t;
        try {
        _rpcsvc.start();
            t =_rpcsvc.connect(_socketAddress, timeout, timeUnit);
        } catch (IOException e ) {
            _rpcsvc.stop();
            throw e;
        }
        return t;
    }

    @Override
    public void close() throws IOException {
        _rpcsvc.stop();
    }
    abstract protected   BUILDER_T getRpcSvcBuilder();


    abstract protected  BUILDER_T getRpcSvcBuilder(OtherParams params);
    public interface OtherParams {

    }
}
