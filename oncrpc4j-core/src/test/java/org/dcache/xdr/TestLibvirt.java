package org.dcache.xdr;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.CompletionHandler;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.libvirt.VirRpcCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLibvirt {
	enum virNetMessageType {
		VIR_NET_CALL(0),
		VIR_NET_REPLY(1),
		VIR_NET_MESSAGE(2),
		VIR_NET_STREAM(3),
		VIR_NET_CALL_WITH_FDS(4),
		VIR_NET_REPLY_WITH_FDS(5);
		private int intValue;

		virNetMessageType(int intValue){
			this.intValue =  intValue;
		}
		static virNetMessageType toValue(int intValue){
			throw new RuntimeException("Unimplemented");
		}
		int  toIntValue(){
			return this.intValue;
		}
		
	};
	enum virNetMessageStatus {
		VIR_NET_OK(0),
		VIR_NET_ERROR(1),
		VIR_NET_CONTINUE(2);
		
		private int intValue;

		virNetMessageStatus(int intValue){
			this.intValue =  intValue;
		}

		public static virNetMessageStatus toValue(int xdrDecodeInt) {
			throw new RuntimeException("Unimplemented");
		}
		int  toIntValue(){
			return this.intValue;
		}
		
	}
	
	class virNetMessageHeader implements XdrAble {
		
		
		int prog;
		int vers;
		int proc;
		virNetMessageType type;
		int serial;
		virNetMessageStatus status;
		@Override
		public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
			prog = xdr.xdrDecodeInt();
			vers = xdr.xdrDecodeInt();
			proc = xdr.xdrDecodeInt();
			type = virNetMessageType.toValue(xdr.xdrDecodeInt());
			serial = xdr.xdrDecodeInt();
			status = virNetMessageStatus.toValue(xdr.xdrDecodeInt());
			
			
		}
		@Override
		public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
			xdr.xdrEncodeInt(prog);
			xdr.xdrEncodeInt(vers);
			xdr.xdrEncodeInt(proc);
			xdr.xdrEncodeInt(type.toIntValue());
			xdr.xdrEncodeInt(serial);
			xdr.xdrEncodeInt(status.toIntValue());
		}
	}
	
	
	@Test
	public void test() throws IOException {
		InetAddress localhost = InetAddress.getByName("127.0.0.1");
		int tcp = IpProtocolType.TCP;
		int port = 16509;
		OncRpcClient client = new OncRpcClient(localhost,tcp,port);
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
	@Test
	public void testProtocol() {
		
	}

}
