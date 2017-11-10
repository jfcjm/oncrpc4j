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
import java.nio.ByteOrder;

import org.dcache.xdr.GrizzlyMemoryManager;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcAuthTypeNone;
import org.dcache.xdr.RpcMessageType;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrString;
import org.dcache.xdr.XdrVoid;
import org.dcache.xdr.model.impl.GenReplyQueue;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.junit.*;
import org.libvirt.GenVirRpcMessageParserTCP2;
import org.libvirt.GenVirRpcProtocolFilter;
import org.libvirt.VirRpcMessage;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class VirRpcMessageParserTCPTest {

    private final static int INVOKE = 0;
    private final static int STOP = 1;
    private FilterChainContext mockedContext;
    private GenVirRpcMessageParserTCP2 tcpParser;
    private GenVirRpcProtocolFilter rpc;

    @Before
    public void setUp() {
        mockedContext = FilterChainContext.create(mock(Connection.class));
        tcpParser = new GenVirRpcMessageParserTCP2();
        rpc = new GenVirRpcProtocolFilter(new GenReplyQueue<>());
    }

    @Test
    public void testNoBuffer() throws IOException {

        assertEquals(STOP, tcpParser.handleRead(mockedContext).type());
    }

    @Test
    public void testEmptyBuffer() throws IOException {
        Buffer b = GrizzlyMemoryManager.allocate(0);
        mockedContext.setMessage(b);

        assertEquals(STOP, tcpParser.handleRead(mockedContext).type());
    }

    @Test
    public void testCompleteMessage() throws IOException, OncRpcException {
        Xdr xdr = new XdrStreamBuilder().build();
        Buffer b = toFragmentedBuffer(xdr, 1024);
        mockedContext.setMessage(b);

        assertEquals(INVOKE, tcpParser.handleRead(mockedContext).type());
    }

    @Test
    public void testPartialMessageMessage() throws IOException, OncRpcException {
        Xdr xdr = new XdrStreamBuilder().build();
        Buffer b = toFragmentedBuffer(xdr, 1024);
        b.limit(b.limit() / 2);
        mockedContext.setMessage(b);

        assertEquals(STOP, tcpParser.handleRead(mockedContext).type());
    }

    @Test
    public void testFragmentedMessageMessage() throws IOException, OncRpcException {
        Xdr xdr = new XdrStreamBuilder().withArgs(new XdrString(new String(new byte[2048]))).build();
        Buffer b = toFragmentedBuffer(xdr, 1024);
        mockedContext.setMessage(b);

        assertEquals(INVOKE, tcpParser.handleRead(mockedContext).type());
        assertEquals(STOP, rpc.handleRead(mockedContext).type());
    }

    private class XdrStreamBuilder {

        int xid = 0;
        int rpcvers = 2;
        int prog = 0;
        int vers = 0;
        int proc = 0;
        RpcAuth auth = new RpcAuthTypeNone();
        XdrAble args = XdrVoid.XDR_VOID;

        public XdrStreamBuilder withArgs(XdrAble args) {
            this.args = args;
            return this;
        }

        public XdrStreamBuilder withAuth(RpcAuth auth) {
            this.auth = auth;
            return this;
        }

        public XdrStreamBuilder withProc(int proc) {
            this.proc = proc;
            return this;
        }

        public XdrStreamBuilder withProg(int prog) {
            this.prog = prog;
            return this;
        }

        public XdrStreamBuilder withRpcvers(int rpcvers) {
            this.rpcvers = rpcvers;
            return this;
        }

        public XdrStreamBuilder withVers(int vers) {
            this.vers = vers;
            return this;
        }

        public XdrStreamBuilder withXid(int xid) {
            this.xid = xid;
            return this;
        }

        public Xdr build() throws OncRpcException, IOException {
            Xdr xdr = new Xdr(Xdr.MAX_XDR_SIZE);
            xdr.beginEncoding();

            VirRpcMessage rpcMessage = new VirRpcMessage(xid, RpcMessageType.CALL);
            //xdr.xdrEncodeInt(rpcvers);
            xdr.xdrEncodeInt(prog);
            xdr.xdrEncodeInt(vers);
            xdr.xdrEncodeInt(proc);
            rpcMessage.xdrEncode(xdr);
            //auth.xdrEncode(xdr);
            args.xdrEncode(xdr);

            xdr.endEncoding();
            return xdr;
        }
    }
    /**
     * RPC fragment record marker mask
     */
    private final static int RPC_LAST_FRAG = 0x00000000;

    private static Buffer toFragmentedBuffer(Xdr xdr, int size) {

        Buffer b = xdr.asBuffer();
        int nfragments = b.remaining() / size + 1;

        /*
         * allocate a new buffer with space for fragment markers;
         */
        Buffer out = GrizzlyMemoryManager.allocate(b.remaining() + nfragments * 4);
        out.order(ByteOrder.BIG_ENDIAN);

        do {
            --nfragments;
            int fragmentSize = Math.min(size, b.remaining());
            int marker = nfragments > 0 ? fragmentSize : fragmentSize | RPC_LAST_FRAG;
            out.putInt(marker);
            out.put(b, b.position(), fragmentSize);
            b.position(b.position() + fragmentSize);
        } while (nfragments > 0);
        return out.rewind();
    }

}
