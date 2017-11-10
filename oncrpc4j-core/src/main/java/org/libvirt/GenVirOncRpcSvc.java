/*******************************************************************************
 * Copyright (C) 2017 INU Champollion, Albi, France
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 ******************************************************************************/package org.libvirt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.root.GenAbstractOncRpcSvc;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GenVirOncRpcSvc extends GenAbstractOncRpcSvc<GenVirOncRpcSvc> implements GenItfVirOncRpcSvc {
    private static final Logger _log = LoggerFactory.getLogger(GenVirOncRpcSvc.class);
    
    private final Map<Transport, PacketWrapperFilter> _transport2wrapper = 
            new HashMap<Transport, PacketWrapperFilter>();
    
    
    public  GenVirOncRpcSvc(GenVirOncRpcSvcBuilder builder) {
        super(builder);
    }
    /* (non-Javadoc)
     * @see org.libvirt.GenItfVirOncRpcSvc#start()
     */
    @Override
    public void start() throws IOException {
        for (Transport t : _transports) {
            if ( !( t instanceof TCPNIOTransport)){
                _log.error("Libvirt does not use UDP");
                throw new RuntimeException("Libvirt does not use UDP transport");
            }
        }
        super.start();
    }
    @Override
    protected GenVirRpcProtocolFilter getRpcProtocolFilter(ReplyQueueItf<GenVirOncRpcSvc> replyQueue) {
        return new GenVirRpcProtocolFilter(replyQueue);
    }

    @Override
    protected void addPostTransportProtocolFilters(FilterChainBuilder filterChain,Transport t) {
        PacketWrapperFilter pwf = new PacketWrapperFilter();
        _transport2wrapper.put(t,pwf);
        filterChain.add(pwf);
    }
    
    @Override
    protected Filter rpcMessageReceiverFor(Transport t) {
        if (t instanceof TCPNIOTransport) {
            return new GenVirRpcMessageParserTCP2();
        }
        throw new RuntimeException("Unsupported transport: " + t.getClass().getName());
    }
    
    
    
    /* (non-Javadoc)
     * @see org.libvirt.GenItfVirOncRpcSvc#setPacketWrapper(org.libvirt.SASLPacketWrapper)
     */
    @Override
    public synchronized void setPacketWrapper(SASLPacketWrapper sc) throws OncRpcException {
        for (Entry<Transport, PacketWrapperFilter> wrapper : _transport2wrapper.entrySet()){
            _log.debug("setting packet wrapper for" + wrapper.getValue());
            wrapper.getValue().setPacketWrapper(sc);
        }
        
    }
    /* (non-Javadoc)
     * @see org.libvirt.GenItfVirOncRpcSvc#setPacketWrapperAfterNextWrite(org.libvirt.SASLPacketWrapper)
     */
    @Override
    public void setPacketWrapperAfterNextWrite(SASLPacketWrapper pw) {
        for (Entry<Transport, PacketWrapperFilter> wrapper : _transport2wrapper.entrySet()){
            _log.debug("setting packet wrapper for" + wrapper.getValue());
            wrapper.getValue().setPacketWrapperAfterNextWrite(pw);
        }
    }
    @Override
    protected void doPostCreationServerActions(Connection<InetSocketAddress> connection) throws IOException {
        // do nothing
        
    }
    @Override
    protected void doPreStopActions() throws IOException {
        
    }
    @Override
    protected void doBeforeStart() throws IOException {
        // TODO Auto-generated method stub
        
    }
    @Override
    protected void addPostRpcProtocolFilter(FilterChainBuilder filterChain) {
        // TODO Auto-generated method stub
        
    }
}
