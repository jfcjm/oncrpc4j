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
package org.dcache.libvirt;

import org.junit.Test;
import org.libvirt.VirRpcCall;

import org.dcache.xdr.BadXdrOncRpcException;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrBuffer;

public class RpcCallTest {

    private Xdr _xdr = new XdrBuffer(1024);
    private VirRpcCall _call = new VirRpcCall(0, _xdr, null);

    @Test(expected=BadXdrOncRpcException.class)
    public void testIncomplete1() throws Exception {
        _xdr.beginEncoding();

        _xdr.xdrEncodeInt(3); // rpc prog
        _xdr.endEncoding();

        _xdr.beginDecoding();
        _call.accept();

    }

    @Test(expected=BadXdrOncRpcException.class)
    public void testIncomplete2() throws Exception {
        _xdr.beginEncoding();

        _xdr.xdrEncodeInt(3); // rpc prog
        _xdr.xdrEncodeInt(0); //rpc vers
        _xdr.endEncoding();

        _xdr.beginDecoding();
        _call.accept();

    }

    //No Xdr exception thrown
    public void testcomplete3() throws Exception {
        _xdr.beginEncoding();

        _xdr.xdrEncodeInt(3); // rpc prog
        _xdr.xdrEncodeInt(0); //rpc vers
        _xdr.xdrEncodeInt(12); //rpc call
        _xdr.endEncoding();

        _xdr.beginDecoding();
        _call.accept();

    }

}