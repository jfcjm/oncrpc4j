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
package org.dcache.xdr;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.dcache.xdr.model.itf.HeaderItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcCall;
import org.dcache.xdr.model.root.AbstractRpcMessage;
public class RpcCallGenericTest {

    

    private Xdr _xdr = new XdrBuffer(1024);
    
    XdrTransportItf t = mock(XdrTransportItf.class);
    AbstractRpcCall _call;
    /**
     * need to use a @Before method as AbstractHeader throws an exception
     * @throws OncRpcException
     * @throws IOException
     */
    @Before
    public void setup() throws OncRpcException, IOException{
        HeaderItf<?> header = new AbstractRpcMessage(_xdr);
        _call = new AbstractRpcCall(header, _xdr, t);
    }
    
    
    @Test(expected=RpcMismatchReply.class)
    public void testBadRpcVerion() throws Exception {
        _xdr.beginEncoding();

        _xdr.xdrEncodeInt(3); // rpc version
        _xdr.endEncoding();

        _xdr.beginDecoding();
        _call.accept();

    }

    @Test(expected=RpcAuthException.class)
    public void testBadAuthType() throws Exception {
        _xdr.beginEncoding();

        _xdr.xdrEncodeInt(2); // rpc version
        _xdr.xdrEncodeInt(127); // prog
        _xdr.xdrEncodeInt(1); // vers
        _xdr.xdrEncodeInt(0); // proc

        _xdr.xdrEncodeInt(100);
        _xdr.endEncoding();

        _xdr.beginDecoding();
        _call.accept();
    }

}