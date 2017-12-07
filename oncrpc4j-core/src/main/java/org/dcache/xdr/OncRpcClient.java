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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.model.itf.OncRpcClientItf;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.root.AbstractOncRpcClient;
/**
 * Pour pouvoir hériter d'abstract : il faut pouvoir associer le protocole IP donc modifier la chaîne d'invocation
 * du builder dès le constructeur ..
 * 
 * @author jmk
 *
 * @param <SVC_T>
 */
public  class OncRpcClient  extends AbstractOncRpcClient<OncRpcSvc,RpcCall,OncRpcSvcBuilder,XdrTransport, RpcReply> {
    protected static final String DEFAULT_SERVICE_NAME = AbstractOncRpcClient.DEFAULT_SERVICE_NAME;
    /**
     * On rajoute le paramètre int protocol, de l'appel pour le moment
     * @param address
     * @param port
     */
    public OncRpcClient(InetAddress address, int protocol, int port) {
        this(new InetSocketAddress(address, port),  protocol, 0, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }
    /**
     * On rajoute le paramètre int protocol, de l'appel pour le moment
     * @param address
     * @param port
     * @param localPort
     */
    public OncRpcClient(InetAddress address, int protocol, int port, int localPort) {
        this(new InetSocketAddress(address, port),  protocol,localPort, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }
    /**
     * On rajoute le paramètre int protocol, de l'appel pour le moment
     * @param address
     * @param port
     * @param localPort
     * @param ioStrategy
     */
    public OncRpcClient(InetAddress address, int protocol, int port, int localPort, IoStrategy ioStrategy) {
        this(new InetSocketAddress(address, port),   protocol, localPort, ioStrategy, DEFAULT_SERVICE_NAME);
    }
    /**
     * On rajoute  paramètre int protocol, de l'appel pour le moment
     * @param address
     * @param port
     * @param localPort
     * @param ioStrategy
     * @param serviceName
     */
    public OncRpcClient(InetAddress address, int protocol, int port, int localPort, IoStrategy ioStrategy, String serviceName) {
        this(new InetSocketAddress(address, port),  protocol, localPort, ioStrategy, serviceName);
    }
    /**
     * On rajoute le paramètre int protocol, de l'appel pour le moment
     * @param socketAddress
     * @param protocol
     */
    public OncRpcClient(InetSocketAddress socketAddress, int protocol) {
        this(socketAddress,   protocol, 0, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }
    
    
    //JMK : type cast
    public OncRpcClient(InetSocketAddress socketAddress, int protocol, int localPort, IoStrategy ioStrategy, String serviceName) {
        super(socketAddress,localPort,ioStrategy,serviceName,new OncCLientParams(protocol));
        //comment passe t'on les nouveaux paramètres du builder (withIpProtocolType(protocol)),with GssSessionManager?
    }
    
    @Override
    protected OncRpcSvcBuilder getRpcSvcBuilder() {
        System.out.println("OK 1");;
        return new OncRpcSvcBuilder();
    }
    @Override
    protected   OncRpcSvcBuilder getRpcSvcBuilder(OtherParams params) {
        return new OncRpcSvcBuilder().withIpProtocolType(((OncCLientParams)params).getProtocol());
    }
    
    protected  static class OncCLientParams implements OtherParams{
       final int _protocol ;
       public OncCLientParams(final int  protocol) {
            _protocol = protocol;
       }
    public int getProtocol() {
        return _protocol;
    }
    }
}
