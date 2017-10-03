package org.dcache.libvirt;

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
import org.dcache.xdr.OncRpcRejectedException;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrString;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.XdrVoid;
import org.glassfish.grizzly.Buffer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;
import org.libvirt.VirOncRpcClient;
import org.libvirt.VirRpcCall;

public class TestLibvirt {
	
	
	
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

        
        
        Buffer buffer = xdr.asBuffer();
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
	
	@Test
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
	
	@Test
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
        assertEquals(136,respLength);
        assertEquals("20008086",Integer.toHexString(xdrResp.xdrDecodeInt()));
        assertEquals(1,xdrResp.xdrDecodeInt()); // version
        assertEquals(1,xdrResp.xdrDecodeInt()); //proc
        assertEquals(1,xdrResp.xdrDecodeInt()); //type
        assertEquals(1,xdrResp.xdrDecodeInt()); //serial
        assertEquals(1,xdrResp.xdrDecodeInt()); //status
        xdrResp.xdrDecodeInt();
        xdrResp.xdrDecodeInt();
        xdrResp.xdrDecodeInt();
        assertEquals("authentication required",xdrResp.xdrDecodeString());
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
	@Test
	public void testVirRPcRaisesExceptionAuth() throws IOException {
		thrown.expect(OncRpcRejectedException.class);
		thrown.expectMessage(JUnitMatchers.containsString("authentication required"));
		InetAddress localhost = InetAddress.getByName("127.0.0.1");
		int tcp = IpProtocolType.TCP;
		int port = 16509;
		OncRpcClient client = new VirOncRpcClient(localhost,tcp,port);
		assertNotNull(client);
		XdrTransport transport = client.connect();
		assertNotNull(transport);
		assertTrue(transport.isOpen());
		int PROGNUM = 0x20008086;
		int PROGVER = 1;
			
		RpcAuth auth = null;
		VirRpcCall virClient = new VirRpcCall(PROGNUM,PROGVER,auth,transport);
		XdrString s = new XdrString("hello");
		XdrVoid reply = new XdrVoid();
		virClient.call(1, s, reply);
	}
	@Test
	public void testVirRPcRaisesExceptionBadProgram() throws IOException {
		thrown.expect(OncRpcRejectedException.class);
		thrown.expectMessage(JUnitMatchers.containsString("Cannot find program"));
		InetAddress localhost = InetAddress.getByName("127.0.0.1");
		int tcp = IpProtocolType.TCP;
		int port = 16509;
		OncRpcClient client = new VirOncRpcClient(localhost,tcp,port);
		assertNotNull(client);
		XdrTransport transport = client.connect();
		assertNotNull(transport);
		assertTrue(transport.isOpen());
		Object attachment = null;
		Xdr xdr = null;
		CompletionHandler handler = null;
		int PROGNUM = 1;
		int PROGVER = 1;
			
		RpcAuth auth = null;
		VirRpcCall virClient = new VirRpcCall(PROGNUM,PROGVER,auth,transport);
		XdrString s = new XdrString("hello");
		XdrVoid reply = new XdrVoid();
		virClient.call(0, s, reply);
	}
}
