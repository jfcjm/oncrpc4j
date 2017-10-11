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

package org.dcache.libvirt.libvirtneeded;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcClient;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrString;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.XdrVoid;
import org.glassfish.grizzly.Buffer;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.hamcrest.CoreMatchers.isA;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;
import org.libvirt.VirOncRpcClient;
import org.libvirt.VirRpcCall;
import org.libvirt.VirRpcRejectedException;
public class LearningVirtProtocolTests {
	
	
	@Category(NoNeedForExternalComponent.class)
	@Test
	public void testEncode () throws UnknownHostException, IOException, InterruptedException {
		Xdr xdr = new Xdr(1000);
		int length = 60;
        xdr.beginEncoding();
        xdr.xdrEncodeInt(length);
        xdr.xdrEncodeInt(0x20008086);//program
        xdr.xdrEncodeInt(1);//version
        xdr.xdrEncodeInt(1);//procedure
        xdr.xdrEncodeInt(0);//type
        xdr.xdrEncodeInt(1);//serial
        xdr.xdrEncodeInt(0);//status
        xdr.xdrEncodeInt(1); //readonly
        String name = "test:///default";
        xdr.xdrEncodeInt(17); //readonly : il y a un problème là...
        xdr.xdrEncodeString(name);
        xdr.xdrEncodeInt(0);

        
        
        Xdr bufXdr = new Xdr(1000);
        ByteBuffer byteBuffer = xdr.asBuffer().toByteBuffer();
        assertEquals(60,byteBuffer.position());
        assertEquals(1000,byteBuffer.limit());
        bufXdr.beginEncoding();
        bufXdr.xdrEncodeByteBuffer(byteBuffer);
        
        bufXdr.endEncoding();
        assertEquals(0,bufXdr.asBuffer().position());
        assertEquals(length+4,bufXdr.asBuffer().limit());
	}
	
	@Test(timeout=3000)
	public void testProtocol() throws UnknownHostException, IOException, InterruptedException {
		try (Socket sock = new Socket(InetAddress.getByName("127.0.0.1"), 16509)) {
		
		//SocketChannel out = sock.getChannel();
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
        DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
        Xdr xdr = new Xdr(1000);
        xdr.beginEncoding();
        xdr.xdrEncodeInt(28);
        xdr.xdrEncodeInt(0x6b656570);//program
        xdr.xdrEncodeInt(1);//version
        xdr.xdrEncodeInt(1);//procedure
        xdr.xdrEncodeInt(0);//type
        xdr.xdrEncodeInt(10);//serial
        xdr.xdrEncodeInt(0);//status
        xdr.endEncoding();
        assertEquals(0,xdr.asBuffer().position());
        assertEquals(28,xdr.asBuffer().limit());
        Buffer buffer = xdr.asBuffer();
        
        out.write(buffer.array(),0,buffer.limit());
        out.flush();
        System.out.println("sent");
        Xdr xdrResp = new Xdr(1000);
        Buffer respBuff = xdrResp.asBuffer();
        inFromServer.read(respBuff.array(),0,1000);
        System.out.println("received");
        xdr.beginDecoding();
        {
        int respLength = xdrResp.xdrDecodeInt();
        assertEquals(168,respLength);
        }
        {
        String program = Integer.toHexString(xdrResp.xdrDecodeInt());
        assertEquals("6b656570",program);
        }
        {
        int version = xdrResp.xdrDecodeInt();
        assertEquals(1,version); 
        }
        {
        int proc =  xdrResp.xdrDecodeInt();
        assertEquals(1,proc); 
        }
        {
        int type =  xdrResp.xdrDecodeInt();
        assertEquals(1,type); 
        }
        {
        int serial =  xdrResp.xdrDecodeInt();
        assertEquals(10,serial); 
        }
        {
        int status =  xdrResp.xdrDecodeInt();
        assertEquals(1,status);
        }
        xdrResp.xdrDecodeInt();
        xdrResp.xdrDecodeInt();
        xdrResp.xdrDecodeInt();
        String message =  xdrResp.xdrDecodeString();
        assertEquals("Cannot find program 1801807216 version 1",message); 
        xdr.endDecoding();
		}
	}
	
	@Test(timeout=3000)
	public void testOpen() throws UnknownHostException, IOException, InterruptedException {
		Socket sock = new Socket(InetAddress.getByName("127.0.0.1"), 16509);
		//SocketChannel out = sock.getChannel();
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
        DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
        Xdr xdr = new Xdr(1000);
        int length = 60;
        xdr.beginEncoding();
        xdr.xdrEncodeInt(length);
        xdr.xdrEncodeInt(0x20008086);//program
        xdr.xdrEncodeInt(1);//version
        xdr.xdrEncodeInt(1);//procedure
        xdr.xdrEncodeInt(0);//type
        xdr.xdrEncodeInt(1);//serial
        xdr.xdrEncodeInt(0);//status
        xdr.xdrEncodeInt(1); //readonly
        String name = "test:///default";
        xdr.xdrEncodeInt(17); //readonly : il y a un problème là...
        xdr.xdrEncodeString(name);
        xdr.xdrEncodeInt(0);
        xdr.endEncoding();
        assertEquals(0,xdr.asBuffer().position());
        assertEquals(length,xdr.asBuffer().limit());
        Buffer buffer = xdr.asBuffer();
        
        out.write(buffer.array(),0,buffer.limit());
        out.flush();
        System.out.println("sent");
        Xdr xdrResp = new Xdr(1000);
        Buffer respBuff = xdrResp.asBuffer();
        inFromServer.read(respBuff.array(),0,1000);
        xdr.beginDecoding();
        int respLength = xdrResp.xdrDecodeInt();
        assertEquals(28,respLength);
        assertEquals("20008086",Integer.toHexString(xdrResp.xdrDecodeInt()));
        assertEquals(1,xdrResp.xdrDecodeInt()); // version
        assertEquals(1,xdrResp.xdrDecodeInt()); //proc
        assertEquals(1,xdrResp.xdrDecodeInt()); //type
        assertEquals(1,xdrResp.xdrDecodeInt()); //serial
        assertEquals(0,xdrResp.xdrDecodeInt()); //status
        
        xdrResp.xdrDecodeInt();
        xdrResp.xdrDecodeInt();
        xdrResp.xdrDecodeInt();
        assertEquals("Cannot find program",xdrResp.xdrDecodeString());
        xdr.endDecoding();
        
        xdr = new Xdr(1000);
        xdr.beginEncoding();
        xdr.xdrEncodeInt(28);
        xdr.xdrEncodeInt(0x20008086);//program
        xdr.xdrEncodeInt(1);//version
        xdr.xdrEncodeInt(2);//procedure
        xdr.xdrEncodeInt(0);//type
        xdr.xdrEncodeInt(1);//serial
        xdr.xdrEncodeInt(0);//status
        xdr.endEncoding();
        assertEquals(0,xdr.asBuffer().position());
        assertEquals(28,xdr.asBuffer().limit());
        buffer = xdr.asBuffer();
        
        out.write(buffer.array(),0,buffer.limit());
        out.flush();
        System.out.println("sent close");
        xdrResp = new Xdr(1000);
        respBuff = xdrResp.asBuffer();
        inFromServer.read(respBuff.array(),0,1000);
        xdr.beginDecoding();
        respLength = xdrResp.xdrDecodeInt();
        System.out.println("received a response of length:" + respLength);
        xdr.endDecoding();
        assertEquals(136,respLength);
        sock.close();
	}
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Test(timeout=3000)
	public void testVirRPcRaisesExceptionAuth() throws IOException {
		thrown.expect(VirRpcRejectedException.class);
		thrown.expectMessage(JUnitMatchers.containsString("authentication required"));
		InetAddress localhost = InetAddress.getByName("127.0.0.1");
		int port = 16509;
		OncRpcClient client = new VirOncRpcClient(localhost,port);
		assertNotNull(client);
		XdrTransport transport = client.connect();
		assertNotNull(transport);
		assertTrue(transport.isOpen());
		int PROGNUM = 0x20008086;
		int PROGVER = 1;
			
		RpcAuth auth = null;
		VirRpcCall virClient = new VirRpcCall(PROGNUM,PROGVER,transport);
		XdrString s = new XdrString("hello");
		XdrVoid reply = new XdrVoid();
		virClient.call(1, s, reply);
	}
	@Test(timeout=3000)
	public void testVirRPcRaisesExceptionBadProgram() throws IOException {
		thrown.expect(VirRpcRejectedException.class);
		//thrown.expectCause(isA(VirRpcRejectedException.class));
		
        thrown.expectCause(new BaseMatcher<VirRpcRejectedException>(){

            @Override
            public boolean matches(Object item) {
                return (item instanceof VirRpcRejectedException) && 
                        ((VirRpcRejectedException) item).getMessage().contains("Cannot find program");
            }

            @Override
            public void describeTo(Description description) {
                // TODO Auto-generated method stub
                
            }
            
        });
		InetAddress localhost = InetAddress.getByName("127.0.0.1");
		int tcp = IpProtocolType.TCP;
		int port = 16509;
		OncRpcClient client = new VirOncRpcClient(localhost,port);
		assertNotNull(client);
		XdrTransport transport = client.connect();
		assertNotNull(transport);
		assertTrue(transport.isOpen());
		Object attachment = null;
		Xdr xdr = null;
		CompletionHandler handler = null;
		int PROGNUM = 1;
		int PROGVER = 1;
		VirRpcCall virClient = new VirRpcCall(PROGNUM,PROGVER,transport);
		XdrString s = new XdrString("hello");
		XdrVoid reply = new XdrVoid();
		virClient.call(0, s, reply);
	}
}
