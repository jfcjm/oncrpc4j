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

import org.dcache.xdr.BadXdrOncRpcException;
import org.dcache.xdr.ReplyQueue;
import org.dcache.xdr.Xdr;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.junit.Before;
import org.junit.Test;
import org.libvirt.VirRpcMessage;
import org.libvirt.VirRpcProtocolFilter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class RpcProtocolFilterTest {

    private final static int INVOKE = 0;
    private final static int STOP = 1;
    private Filter filter;
    private FilterChainContext mockedContext;

    @Before
    public void setUp() {
        filter = new VirRpcProtocolFilter( new ReplyQueue());
        mockedContext = FilterChainContext.create(mock(Connection.class));
    }

    @Test
    public void testEventMessageType() throws IOException {
        mockedContext.setMessage( createEventMessage() );
        assertEquals(STOP, filter.handleRead(mockedContext).type());
    }

    @Test
    public void testStreamMessageType() throws IOException {
        mockedContext.setMessage( createStreamMessage() );
        assertEquals(STOP, filter.handleRead(mockedContext).type());
    }

    @Test
    public void testBadMessageType() throws IOException {
        mockedContext.setMessage( createBadXdr() );
        assertEquals(STOP, filter.handleRead(mockedContext).type());
    }

    private Object createStreamMessage() {
        return createXdr(3);
    }
    
    
    private Object createEventMessage() {
        return createXdr(2);
    }

    private Xdr createBadXdr() throws BadXdrOncRpcException {
        return createXdr(4);
    }

    private Xdr createXdr(int type) {
        Xdr xdr = new Xdr(32);
        xdr.beginEncoding();
        VirRpcMessage rpcMessage = new VirRpcMessage(1, type); // xdr, type 0 = call, 1 = reply, 2 = not allowed
        xdr.xdrEncodeInt(0); //program
        xdr.xdrEncodeInt(0); //version
        xdr.xdrEncodeInt(0); //proc
        rpcMessage.xdrEncode(xdr);
        return xdr;
        
    }
    
}
