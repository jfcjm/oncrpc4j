package org.dcache.libvirt;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;

import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

import org.dcache.xdr.OncRpcException;
import org.glassfish.grizzly.memory.ByteBufferWrapper;
import org.junit.Test;
import org.libvirt.SASLPacketWrapper;

public class SASLPacketWrapperTest {

	@Test
	public void baseTestUnwrap() throws SaslException, OncRpcException {
		SaslClient sc = mock(SaslClient.class);
		byte[] mockedResult = new byte[12];
		when(sc.unwrap(any(), anyInt(), anyInt())).thenReturn(mockedResult);
		SASLPacketWrapper wrapper = new SASLPacketWrapper(sc);
		fail();
		/* byte[] res = wrapper.unwrap(new byte[0]);
		assertEquals(res,mockedResult);
		*/
	}

	@Test
	public void basettestwrap() throws SaslException, OncRpcException {
		SaslClient sc = mock(SaslClient.class);
		byte[] mockedResult = new byte[12];
		when(sc.wrap(any(), anyInt(), anyInt())).thenReturn(mockedResult);
		SASLPacketWrapper wrapper = new SASLPacketWrapper(sc);
		ByteBufferWrapper res = new ByteBufferWrapper(ByteBuffer.wrap(new byte[0]));
		assertEquals(mockedResult,res.array());
	}

}
