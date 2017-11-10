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

import java.net.InetAddress;
import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcClient;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcSvcBuilder;


/**
 * 
 * AU contraire de OncRpcClient, les constructeurs ne nécessitent pas l'utilisation
 * du paramétre prtocole puisque le seulprotocole utilisé par libvirt est TCP.
 * @author jmk
 *
 */
@Deprecated
public class VirOncRpcClient extends OncRpcClient {
    final static int libvirtTransportProtocol = IpProtocolType.TCP;
    
    
    public VirOncRpcClient(InetAddress address, int port) {
        this(address,port,0,IoStrategy.SAME_THREAD,null);
    }
    public VirOncRpcClient(InetAddress host,  int port, int localPort, IoStrategy ioStrategy, String serviceName) {
       super(host,libvirtTransportProtocol,port,localPort,ioStrategy,serviceName);
    }
    protected  OncRpcSvcBuilder getOncRpcSvcBuilder() {
         return new VirOncRpcSvcBuilder();
    }
    public synchronized void setPacketWrapper(SASLPacketWrapper sc) throws OncRpcException {
        ((VirOncRpcSvc) _rpcsvc).setPacketWrapper(sc);
    }
}
