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
 ******************************************************************************/

package org.dcache.libvirt;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.dcache.xdr.IpProtocolType;
import org.junit.After;
import org.junit.Test;
import org.libvirt.GenVirOncRpcSvc;
import org.libvirt.GenVirOncRpcSvcBuilder;
import static org.junit.Assert.*;

public class OncRpcSvcTest {

    private GenVirOncRpcSvc svc;
	

    @Test(expected=RuntimeException.class)
    public void testBindToInterface() throws IOException {
        svc = new GenVirOncRpcSvcBuilder()
                .withTCP()
                .withMinPort(0)
                .withMinPort(4096)
                .withBindAddress("127.0.0.1")
                .build();
        svc.start();

        InetSocketAddress tcpSocketAddresses = svc.getInetSocketAddress(IpProtocolType.TCP);
        InetSocketAddress udpSocketAddresses = svc.getInetSocketAddress(IpProtocolType.UDP);
        assertTrue(!tcpSocketAddresses.getAddress().isAnyLocalAddress());
        assertTrue(!udpSocketAddresses.getAddress().isAnyLocalAddress());
    }

    @Test(expected=RuntimeException.class)
    public void testNotBindToInterface() throws IOException {
        svc =  new GenVirOncRpcSvcBuilder()
                .withTCP()
                .withMinPort(0)
                .withMinPort(4096)
                .build();
        svc.start();

        InetSocketAddress tcpSocketAddresses = svc.getInetSocketAddress(IpProtocolType.TCP);
        InetSocketAddress udpSocketAddresses = svc.getInetSocketAddress(IpProtocolType.UDP);
        assertTrue(tcpSocketAddresses.getAddress().isAnyLocalAddress());
        assertTrue(udpSocketAddresses.getAddress().isAnyLocalAddress());
    }
	
	
    @After
    public void tearDown() throws IOException {
        if (null != svc) svc.stop();
    }
}
